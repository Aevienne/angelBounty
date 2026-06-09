package me.angelique.angelBounty.gui;

import me.angelique.angelBounty.AngelBounty;
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

        switch (event.getSlot()) {
            case 47 -> { // Post contract help
                player.closeInventory();
                player.sendMessage("\u00A78[\u00A74AngelBounty\u00A78] \u00A7e/contract post <PLAYER_KILL|STRUCTURE_DAMAGE|SHIPMENT_INTERCEPT|ESCORT> <target> <reward>");
            }
            case 51 -> player.closeInventory();
            case 20,21,22,23,24,25 -> {
                player.closeInventory();
                player.chat("/wanted");
            }
        }
    }
}
