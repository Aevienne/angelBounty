package me.angelique.angelBounty.listener;

import me.angelique.angelBounty.service.WantedService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class PlayerConnectionListener implements Listener {

    private final WantedService wantedService;

    public PlayerConnectionListener(WantedService wantedService) {
        this.wantedService = wantedService;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        wantedService.handleJoin(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        wantedService.handleQuit(event.getPlayer());
    }
}
