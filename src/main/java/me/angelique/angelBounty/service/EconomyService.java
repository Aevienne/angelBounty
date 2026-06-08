package me.angelique.angelBounty.service;

import me.angelique.angelBounty.AngelBounty;
import me.angelique.angelBounty.config.PluginConfig;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

public final class EconomyService {

    private final AngelBounty plugin;
    private final PluginConfig config;
    private Economy economy;

    public EconomyService(AngelBounty plugin, PluginConfig config) {
        this.plugin = plugin;
        this.config = config;
        setup();
    }

    private void setup() {
        if (!config.isEconomyEnabled()) {
            return;
        }
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            if (config.isVaultRequired()) {
                plugin.getLogger().warning("Vault not found. Disabling plugin because vault-required=true.");
                Bukkit.getPluginManager().disablePlugin(plugin);
            }
            return;
        }
        RegisteredServiceProvider<Economy> registration = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (registration == null) {
            if (config.isVaultRequired()) {
                plugin.getLogger().warning("Vault economy provider not found. Disabling plugin because vault-required=true.");
                Bukkit.getPluginManager().disablePlugin(plugin);
            }
            return;
        }
        this.economy = registration.getProvider();
    }

    public boolean payout(OfflinePlayer player, double amount) {
        return deposit(player, amount);
    }

    public boolean has(OfflinePlayer player, double amount) {
        if (player == null || economy == null) return false;
        return economy.has(player, amount);
    }

    public boolean withdraw(OfflinePlayer player, double amount) {
        if (player == null || economy == null) return false;
        EconomyResponse response = economy.withdrawPlayer(player, amount);
        return response != null && response.transactionSuccess();
    }

    private boolean deposit(OfflinePlayer player, double amount) {
        if (player == null || amount <= 0.0D || economy == null) {
            return false;
        }
        EconomyResponse response = economy.depositPlayer(player, amount);
        return response != null && response.transactionSuccess();
    }

    public String format(double amount) {
        if (economy != null) {
            return economy.format(amount);
        }
        return String.format("%.2f", amount);
    }
}
