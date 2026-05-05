package me.angelique.angelBounty.listener;

import me.angelique.angelBounty.config.PluginConfig;
import me.angelique.angelBounty.service.WantedService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public final class PlayerDeathListener implements Listener {

    private final WantedService wantedService;
    private final PluginConfig config;

    public PlayerDeathListener(WantedService wantedService, PluginConfig config) {
        this.wantedService = wantedService;
        this.config = config;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getPlayer();
        Player killer = victim.getKiller();

        if (!config.isWorldEnabled(victim.getWorld())) {
            return;
        }

        if (killer != null && killer != victim) {
            wantedService.recordPlayerKill(killer, victim);
        }

        if (wantedService.getStars(victim) > 0) {
            wantedService.handleWantedDeath(killer, victim);
        }
    }
}
