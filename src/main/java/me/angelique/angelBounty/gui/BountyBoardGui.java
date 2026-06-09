package me.angelique.angelBounty.gui;

import me.angelique.angelBounty.AngelBounty;
import me.angelique.angelBounty.service.ContractService;
import me.angelique.angelBounty.service.WantedService;
import me.angelique.angelBounty.model.ContractData;
import me.angelique.angelNCore.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

public final class BountyBoardGui {

    public static final String TITLE = TextUtil.color("&8Bounty Board &7\u2014 &4Wanted & Contracts");
    static final int SIZE = 54;

    private BountyBoardGui() {}

    public static void open(Player player, AngelBounty plugin) {
        WantedService ws = plugin.getWantedService();
        ContractService cs = plugin.getContractService();
        Inventory inv = Bukkit.createInventory(null, SIZE, TITLE);
        fillBorder(inv);

        // Header
        inv.setItem(4, item(Material.WITHER_SKELETON_SKULL, "&4Bounty Board",
                "&7View wanted players & open contracts",
                "&7Post bounties and accept mercenary work"));

        // Wanted list (slots 19-25)
        inv.setItem(19, item(Material.PAPER, "&4Most Wanted",
                "&7Top wanted players by star level"));
        List<OfflinePlayer> topWanted = ws.getTopWanted(5);
        int slot = 20;
        for (OfflinePlayer wp : topWanted) {
            int stars = ws.getStars(wp);
            if (stars <= 0) continue;
            double reward = ws.calculateReward(stars);
            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            ItemMeta sm = skull.getItemMeta();
            if (sm != null) {
                sm.setDisplayName(TextUtil.color("&c" + (wp.getName() != null ? wp.getName() : wp.getUniqueId().toString().substring(0,8))));
                sm.setLore(Arrays.asList(
                        TextUtil.color("&4Stars: " + ws.getStarsBar(stars)),
                        TextUtil.color("&6Bounty: $" + String.format("%.2f", reward)),
                        "",
                        TextUtil.color("&eClick to track this target")
                ));
                skull.setItemMeta(sm);
            }
            if (slot <= 25) inv.setItem(slot++, skull);
        }

        // Open contracts (slots 28-34)
        inv.setItem(28, item(Material.BOOK, "&6Open Contracts",
                "&7Available mercenary work"));
        List<ContractData> openContracts = cs.getOpenContracts();
        slot = 29;
        for (ContractData c : openContracts) {
            if (slot > 34) break;
            Material icon = switch (c.getType()) {
                case PLAYER_KILL -> Material.DIAMOND_SWORD;
                case STRUCTURE_DAMAGE -> Material.TNT;
                case SHIPMENT_INTERCEPT -> Material.CHEST_MINECART;
                case ESCORT -> Material.IRON_CHESTPLATE;
            };
            inv.setItem(slot++, item(icon, "&e" + c.getType().name().replace("_", " "),
                    "&7Target: &f" + c.getTarget(),
                    "&7Reward: &a$" + String.format("%.2f", c.getReward()),
                    "&7By: &f" + c.getPosterName(),
                    "&8ID: " + c.getId().substring(0,6),
                    "",
                    "&eClick to accept"));
        }

        // My stats (slot 40)
        int myStars = ws.getStars(player);
        inv.setItem(40, item(Material.IRON_SWORD, "&6Your Status",
                "&4Wanted Level: " + ws.getStarsBar(myStars),
                "&6Bounty on you: $" + String.format("%.2f", ws.calculateReward(myStars)),
                "",
                "&7Use &e/contract post <type> <target> <reward>",
                "&7to create a contract"));

        // Actions
        inv.setItem(47, item(Material.EMERALD, "&aPost Contract",
                "&7Create a new mercenary contract",
                "",
                "&eClick for help"));
        inv.setItem(51, item(Material.BARRIER, "&cClose", "&7Close the board"));

        player.openInventory(inv);
    }

    static void fillBorder(Inventory inv) {
        ItemStack glass = pane(Material.BLACK_STAINED_GLASS_PANE);
        for (int i = 0; i < SIZE; i++) inv.setItem(i, glass);
    }

    static ItemStack item(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(TextUtil.color(name));
            meta.setLore(Arrays.stream(lore).map(TextUtil::color).toList());
            item.setItemMeta(meta);
        }
        return item;
    }

    static ItemStack pane(Material mat) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) { meta.setDisplayName(" "); item.setItemMeta(meta); }
        return item;
    }
}
