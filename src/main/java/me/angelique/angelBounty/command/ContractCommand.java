package me.angelique.angelBounty.command;

import me.angelique.angelBounty.AngelBounty;
import me.angelique.angelBounty.model.ContractData;
import me.angelique.angelBounty.service.ContractService;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.List;

public class ContractCommand implements CommandExecutor, TabCompleter {

    private final AngelBounty plugin;

    public ContractCommand(AngelBounty plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }

        ContractService cs = plugin.getContractService();
        if (cs == null) { player.sendMessage("§cContract service unavailable."); return true; }

        if (args.length == 0 || args[0].equalsIgnoreCase("list")) {
            var contracts = cs.getOpenContracts();
            player.sendMessage("§6=== Open Contracts ===");
            for (ContractData c : contracts) {
                player.sendMessage("§e" + c.getId() + " §7| §f" + c.getType().name().replace("_"," ") +
                    " §7| Target: §c" + c.getTarget() + " §7| §a$" + String.format("%.2f", c.getReward()) +
                    " §7| by " + c.getPosterName());
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("post")) {
            if (args.length < 4) {
                player.sendMessage("§cUsage: /contract post <type> <target> <reward>");
                player.sendMessage("§7Types: PLAYER_KILL, STRUCTURE_DAMAGE, SHIPMENT_INTERCEPT, ESCORT");
                return true;
            }
            ContractData.Type type;
            try { type = ContractData.Type.valueOf(args[1].toUpperCase()); }
            catch (Exception e) { player.sendMessage("§cInvalid type."); return true; }
            String target = args[2];
            double reward = Double.parseDouble(args[3]);

            if (!plugin.getEconomyService().has(player, reward)) {
                player.sendMessage("§cInsufficient funds.");
                return true;
            }
            plugin.getEconomyService().withdraw(player, reward);
            cs.post(player.getUniqueId(), player.getName(), target, type, reward);
            return true;
        }

        if (args[0].equalsIgnoreCase("accept")) {
            if (args.length < 2) { player.sendMessage("§cUsage: /contract accept <contractId>"); return true; }
            boolean ok = cs.accept(args[1], player.getUniqueId());
            player.sendMessage(ok ? "§aContract accepted!" : "§cFailed to accept.");
            return true;
        }

        if (args[0].equalsIgnoreCase("complete")) {
            if (args.length < 2) { player.sendMessage("§cUsage: /contract complete <contractId>"); return true; }
            boolean ok = cs.complete(args[1], player.getUniqueId());
            player.sendMessage(ok ? "§aContract completed!" : "§cFailed to complete.");
            return true;
        }

        player.sendMessage("§e/contract list §7| §e/contract post <type> <target> <reward> §7| §e/contract accept <id> §7| §e/contract complete <id>");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length == 1) return List.of("list", "post", "accept", "complete");
        if (args.length == 2 && args[0].equalsIgnoreCase("post"))
            return List.of("PLAYER_KILL", "STRUCTURE_DAMAGE", "SHIPMENT_INTERCEPT", "ESCORT");
        return List.of();
    }
}
