package me.angelique.angelBounty.gui;

import me.angelique.angelBounty.AngelBounty;
import me.angelique.angelBounty.model.ContractData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class BountyBoardListener implements Listener {

    private final AngelBounty plugin;

    public BountyBoardListener(AngelBounty plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!event.getView().getTitle().equals(BountyBoardGui.TITLE)) return;

        event.setCancelled(true);
        int slot = event.getSlot();

        if (slot == 51) { player.closeInventory(); return; }
        if (slot == 47) {
            player.closeInventory();
            player.sendMessage(me.angelique.angelNCore.util.TextUtil.color("&e/contract post <type> <target> <reward>"));
            player.sendMessage(me.angelique.angelNCore.util.TextUtil.color("&7Types: PLAYER_KILL, STRUCTURE_DAMAGE, SHIPMENT_INTERCEPT, ESCORT"));
            return;
        }
        // Wanted skulls (slots 20-25)
        if (slot >= 20 && slot <= 25) { player.closeInventory(); player.chat("/wanted"); return; }

        // Contract accept (slots 28-34)
        java.util.List<ContractData> contracts = BountyBoardGui.contractCache.get(player.getUniqueId());
        if (contracts != null) {
            int idx = slot - 29;
            if (idx >= 0 && idx < contracts.size()) {
                ContractData c = contracts.get(idx);
                if (plugin.getContractService().accept(c.getId(), player.getUniqueId())) {
                    player.sendMessage(me.angelique.angelNCore.util.TextUtil.color("&aContract accepted! Target: &c" + c.getTarget()));
                } else {
                    player.sendMessage(me.angelique.angelNCore.util.TextUtil.color("&cCannot accept this contract."));
                }
                player.closeInventory();
            }
        }
    }
}
