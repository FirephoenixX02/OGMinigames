package me.firephoenix.ps3minigames.listener;

import me.firephoenix.ps3minigames.PS3Minigames;
import me.firephoenix.ps3minigames.states.LobbyState;
import me.firephoenix.ps3minigames.util.GameUtil;
import me.firephoenix.ps3minigames.util.Timer;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.UUID;

/**
 * @author NieGestorben
 * Copyright© (c) 2023, All Rights Reserved.
 */
public class JoinQuitListener implements Listener {

    public FileConfiguration config = PS3Minigames.INSTANCE.getConfig();

    public PS3Minigames plugin = PS3Minigames.INSTANCE;

    public Timer gameTimer;

    public ArrayList<UUID> lobbyPlayers = new ArrayList<>();

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        player.teleport(new Location(plugin.getServer().getWorld(config.getString("spawn-lobby.world")), config.getDouble("spawn-lobby.x"), config.getDouble("spawn-lobby.y"), config.getDouble("spawn-lobby.z"), (float) config.getDouble("spawn-lobby.yaw"), (float) config.getDouble("spawn-lobby.pitch")));
        player.setHealth(20);
        player.setFoodLevel(20);
        player.getInventory().clear();
        player.setGameMode(GameMode.SURVIVAL);

        // Send all players which are in the lobby the join message
        for (Player player1 : plugin.getLobby().getPlayers()) {
            player1.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("messages.join").replace("%player%", player.getDisplayName())));
        }
        // Send joined player the join message, because joinevent is 1 tick before the player gets added to the world player list
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("messages.join").replace("%player%", player.getDisplayName())));
        if (plugin.getLobby().getPlayers().size() == 0) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("messages.needed-players")));
            for (Player player1 : plugin.getLobby().getPlayers()) {
                player1.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("messages.needed-players")));
            }
        } else {
            if (plugin.getLobby().getPlayers().size() >= 3) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("messages.game-start").replace("%seconds%", "10")));
                for (Player player1 : plugin.getLobby().getPlayers()) {
                    player1.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("messages.game-start").replace("%seconds%", "10")));
                }
            } else {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("messages.game-start").replace("%seconds%", "25")));
                for (Player player1 : plugin.getLobby().getPlayers()) {
                    player1.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("messages.game-start").replace("%seconds%", "25")));
                }
            }
            if (plugin.getLobbyState() == LobbyState.IDLE) {
                plugin.setLobbyState(LobbyState.STARTING);
                lobbyPlayers.add(player.getUniqueId());
                plugin.getLobby().getPlayers().forEach(player1 -> lobbyPlayers.add(player1.getUniqueId()));

                gameTimer.start();
                gameTimer = new Timer(plugin.getLobby().getPlayers().size() >= 3 ? 10 : 25, plugin);
                gameTimer.whenComplete(() -> PS3Minigames.INSTANCE.getGameUtil().startNewGame(lobbyPlayers, plugin.getServer().getWorld("cavern")));
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        lobbyPlayers.remove(e.getPlayer().getUniqueId());
    }

}
