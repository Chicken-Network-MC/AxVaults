package com.artillexstudios.axvaults.listeners;

import com.artillexstudios.axvaults.vaults.VaultManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlayerListeners implements Listener {

    private static final Logger log = LoggerFactory.getLogger(PlayerListeners.class);

    public PlayerListeners() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            VaultManager.getPlayer(player);
        }
    }

    @EventHandler
    public void onQuit(@NotNull PlayerQuitEvent event) {
        VaultManager.getPlayer(event.getPlayer()).thenAccept((vaultPlayer) -> {
            log.info("Unloading player: " + event.getPlayer().getName());
            vaultPlayer.save();
            vaultPlayer.getVaultMap().values().forEach(VaultManager::removeVault);
            VaultManager.getPlayers().remove(event.getPlayer().getUniqueId());
        });
    }
}
