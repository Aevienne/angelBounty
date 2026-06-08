package me.angelique.angelBounty.service;

import me.angelique.angelBounty.AngelBounty;
import me.angelique.angelBounty.model.ContractData;
import me.angelique.angelNCore.events.BountyCompletedEvent;
import me.angelique.angelNCore.events.EventBus;
import org.bukkit.Bukkit;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ContractService {

    private final AngelBounty plugin;
    private final Map<String, ContractData> contracts = new ConcurrentHashMap<>();

    public ContractService(AngelBounty plugin) { this.plugin = plugin; }

    public String post(UUID poster, String posterName, String target, ContractData.Type type, double reward) {
        String id = UUID.randomUUID().toString().substring(0, 8);
        ContractData c = new ContractData(id, poster, posterName, target, type, reward);
        contracts.put(id, c);
        Bukkit.broadcastMessage("§6[Contracts] §e" + posterName + " posted a " + type.name().replace("_"," ") +
            " contract on §c" + target + " §7(§a$" + String.format("%.2f", reward) + "§7) §8[" + id + "]");
        return id;
    }

    public boolean accept(String contractId, UUID acceptor) {
        ContractData c = contracts.get(contractId);
        if (c == null || c.getStatus() != ContractData.Status.OPEN || c.isExpired()) return false;
        if (c.getPosterUUID().equals(acceptor)) return false;
        c.setAcceptedBy(acceptor);
        return true;
    }

    public boolean complete(String contractId, UUID completer) {
        ContractData c = contracts.get(contractId);
        if (c == null || c.getStatus() != ContractData.Status.ACCEPTED) return false;
        if (!completer.equals(c.getAcceptedBy())) return false;
        c.setStatus(ContractData.Status.COMPLETED);
        plugin.getEconomyService().payout(Bukkit.getOfflinePlayer(completer), c.getReward());
        EventBus.publish(new BountyCompletedEvent(contractId, completer.toString(), c.getTarget(), c.getReward()));
        Bukkit.broadcastMessage("§6[Contracts] §a" + Bukkit.getOfflinePlayer(completer).getName() +
            " completed contract §e" + contractId + " §7- $" + String.format("%.2f", c.getReward()));
        return true;
    }

    public List<ContractData> getOpenContracts() {
        List<ContractData> list = new ArrayList<>();
        for (ContractData c : contracts.values()) {
            if (c.isExpired()) { c.setStatus(ContractData.Status.EXPIRED); continue; }
            if (c.getStatus() == ContractData.Status.OPEN) list.add(c);
        }
        return list;
    }

    public List<ContractData> getMyContracts(UUID playerUUID) {
        List<ContractData> list = new ArrayList<>();
        for (ContractData c : contracts.values()) {
            if (c.getAcceptedBy() != null && c.getAcceptedBy().equals(playerUUID)) list.add(c);
        }
        return list;
    }

    public ContractData getContract(String id) { return contracts.get(id); }

    public void expireOld() {
        contracts.values().removeIf(c -> c.isExpired() && c.getStatus() == ContractData.Status.OPEN);
    }
}
