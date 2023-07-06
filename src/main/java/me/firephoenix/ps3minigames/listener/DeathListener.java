package me.firephoenix.ps3minigames.listener;

import me.firephoenix.ps3minigames.PS3Minigames;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.Objects;

/**
 * @author NieGestorben
 * Copyright© (c) 2023, All Rights Reserved.
 */
public class DeathListener implements Listener {

    public FileConfiguration config = PS3Minigames.INSTANCE.getConfig();

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player player = e.getEntity().getPlayer();
        Player killer = e.getEntity().getKiller();

        player.getWorld().getPlayers().forEach(players -> players.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("messages.player-death").replace("%player%", player.getDisplayName()).replace("%cause%", killer == null ? "enviroment" : killer.getDisplayName()))));
        if (player.getWorld().getPlayers().size() == 1) {
            PS3Minigames.INSTANCE.getGameUtil().stopGame(Objects.requireNonNull(PS3Minigames.INSTANCE.getGameUtil().getGameByWorld(player.getWorld())));
        }
    }
}
