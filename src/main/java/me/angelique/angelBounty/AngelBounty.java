package me.angelique.angelBounty;

import me.angelique.angelBounty.command.ContractCommand;
import me.angelique.angelBounty.command.WantedCommand;
import me.angelique.angelBounty.config.PluginConfig;
import me.angelique.angelBounty.listener.PlayerConnectionListener;
import me.angelique.angelBounty.listener.PlayerDeathListener;
import me.angelique.angelBounty.service.ContractService;
import me.angelique.angelBounty.service.EconomyService;
import me.angelique.angelBounty.service.WantedService;
import me.angelique.angelBounty.storage.WantedStorage;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public final class AngelBounty extends JavaPlugin {

    private PluginConfig pluginConfig;
    private EconomyService economyService;
    private WantedStorage wantedStorage;
    private WantedService wantedService;
    private ContractService contractService;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadPlugin();
    }

    public void reloadPlugin() {
        HandlerList.unregisterAll(this);
        reloadConfig();

        this.pluginConfig = new PluginConfig(getConfig());
        this.economyService = new EconomyService(this, pluginConfig);
        this.wantedStorage = new WantedStorage(this);

        if (this.wantedService != null) {
            this.wantedService.shutdown();
        }

        this.wantedService = new WantedService(this, pluginConfig, economyService, wantedStorage);
        this.wantedService.initialize();
        this.contractService = new ContractService(this);

        Bukkit.getPluginManager().registerEvents(new PlayerDeathListener(wantedService, pluginConfig), this);
        Bukkit.getPluginManager().registerEvents(new PlayerConnectionListener(wantedService), this);

        PluginCommand wantedCommand = getCommand("wanted");
        if (wantedCommand != null) {
            WantedCommand executor = new WantedCommand(wantedService, pluginConfig, this);
            wantedCommand.setExecutor(executor);
            wantedCommand.setTabCompleter(executor);
        }

        PluginCommand contractCmd = getCommand("contract");
        if (contractCmd != null) {
            ContractCommand cc = new ContractCommand(this);
            contractCmd.setExecutor(cc);
            contractCmd.setTabCompleter(cc);
        }
    }

    @Override
    public void onDisable() {
        if (wantedService != null) {
            wantedService.shutdown();
        }
    }

    public ContractService getContractService() { return contractService; }
    public EconomyService getEconomyService() { return economyService; }
}
