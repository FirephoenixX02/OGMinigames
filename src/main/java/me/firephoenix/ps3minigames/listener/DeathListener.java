package me.firephoenix.ps3minigames.listener;

import me.firephoenix.ps3minigames.PS3Minigames;
import me.firephoenix.ps3minigames.game.Game;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

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
        player.setGameMode(GameMode.SPECTATOR);
        Game game = PS3Minigames.INSTANCE.getGameUtil().getGameByWorld(player.getWorld());
        if (game != null) {
            game.getPlayers().remove(player.getUniqueId());
            if (game.getPlayers().size() <= 1) {
                PS3Minigames.INSTANCE.getGameUtil().stopGame(game);
            }
        }
    }
}
