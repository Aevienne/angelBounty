package me.angelique.angelBounty.command;

import me.angelique.angelBounty.AngelBounty;
import me.angelique.angelBounty.config.PluginConfig;
import me.angelique.angelBounty.service.WantedService;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class WantedCommand implements CommandExecutor, TabCompleter {

    private final WantedService wantedService;
    private final PluginConfig config;
    private final AngelBounty plugin;

    public WantedCommand(WantedService wantedService, PluginConfig config, AngelBounty plugin) {
        this.wantedService = wantedService;
        this.config = config;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("/wanted <player>");
                return true;
            }
            sendStatus(sender, player);
            return true;
        }

        if (args.length == 1 && "top".equalsIgnoreCase(args[0])) {
            sender.sendMessage(config.getMessageTopHeader());
            int index = 1;
            for (OfflinePlayer player : wantedService.getTopWanted(10)) {
                int stars = wantedService.getStars(player);
                String name = player.getName() == null ? player.getUniqueId().toString() : player.getName();
                sender.sendMessage("§e" + index + ". §f" + name + " §7- §c" + stars + " star(s) §7- §a$" + wantedService.calculateReward(stars));
                index++;
            }
            if (index == 1) {
                sender.sendMessage("§7Nobody is wanted right now.");
            }
            return true;
        }

        if (args.length == 1 && !("set".equalsIgnoreCase(args[0]) || "add".equalsIgnoreCase(args[0]) || "clear".equalsIgnoreCase(args[0]) || "reload".equalsIgnoreCase(args[0]) || "top".equalsIgnoreCase(args[0]))) {
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
            sendStatus(sender, target);
            return true;
        }

        if (!sender.hasPermission("angelbounty.admin")) {
            sender.sendMessage("§cYou do not have permission.");
            return true;
        }

        if (args.length == 1 && "reload".equalsIgnoreCase(args[0])) {
            plugin.reloadPlugin();
            sender.sendMessage("§aAngelBounty reloaded.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§e/wanted set <player> <stars>");
            sender.sendMessage("§e/wanted add <player> <stars>");
            sender.sendMessage("§e/wanted clear <player>");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        if ("clear".equalsIgnoreCase(args[0])) {
            wantedService.clear(target);
            sender.sendMessage("§aWanted level cleared for " + safeName(target, args[1]));
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage("§cMissing star value.");
            return true;
        }

        int amount;
        try {
            amount = Integer.parseInt(args[2]);
        } catch (NumberFormatException exception) {
            sender.sendMessage("§cStars must be a number.");
            return true;
        }

        if ("set".equalsIgnoreCase(args[0])) {
            wantedService.setStars(target, amount);
            sender.sendMessage("§aSet wanted stars for " + safeName(target, args[1]) + " to " + amount);
            return true;
        }

        if ("add".equalsIgnoreCase(args[0])) {
            wantedService.addStars(target, amount);
            sender.sendMessage("§aAdded wanted stars for " + safeName(target, args[1]));
            return true;
        }

        return true;
    }

    private void sendStatus(CommandSender sender, OfflinePlayer target) {
        int stars = wantedService.getStars(target);
        sender.sendMessage(config.getMessageTargetStatus()
                .replace("{player}", safeName(target, "Unknown"))
                .replace("{stars}", String.valueOf(stars))
                .replace("{reward}", String.format("%.2f", wantedService.calculateReward(stars))));
    }

    private String safeName(OfflinePlayer player, String fallback) {
        return player.getName() == null ? fallback : player.getName();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();
        if (args.length == 1) {
            suggestions.add("top");
            suggestions.add("set");
            suggestions.add("add");
            suggestions.add("clear");
            suggestions.add("reload");
            Bukkit.getOnlinePlayers().forEach(player -> suggestions.add(player.getName()));
        } else if (args.length == 2 && ("set".equalsIgnoreCase(args[0]) || "add".equalsIgnoreCase(args[0]) || "clear".equalsIgnoreCase(args[0]))) {
            Bukkit.getOnlinePlayers().forEach(player -> suggestions.add(player.getName()));
        } else if (args.length == 3 && ("set".equalsIgnoreCase(args[0]) || "add".equalsIgnoreCase(args[0]))) {
            for (int i = 0; i <= config.getMaxStars(); i++) {
                suggestions.add(String.valueOf(i));
            }
        }
        return suggestions;
    }
}
