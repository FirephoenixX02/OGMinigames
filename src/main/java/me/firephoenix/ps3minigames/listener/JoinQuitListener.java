package me.firephoenix.ps3minigames.listener;

import me.firephoenix.ps3minigames.PS3Minigames;
import me.firephoenix.ps3minigames.game.Game;
import me.firephoenix.ps3minigames.states.LobbyState;
import me.firephoenix.ps3minigames.util.GameUtil;
import me.firephoenix.ps3minigames.util.Timer;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
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

        player.teleport(new Location(
                plugin.getServer().getWorld(config.getString("spawn-lobby.world")),
                config.getDouble("spawn-lobby.x"),
                config.getDouble("spawn-lobby.y"),
                config.getDouble("spawn-lobby.z"),
                (float) config.getDouble("spawn-lobby.yaw"),
                (float) config.getDouble("spawn-lobby.pitch")
        ));

        GameUtil.resetInventory(player);

        // Send all players in the lobby the join message
        String joinMessage = ChatColor.translateAlternateColorCodes('&', config.getString("messages.join").replace("%player%", player.getDisplayName()));
        for (Player player1 : plugin.getLobby().getPlayers()) {
            player1.sendMessage(joinMessage);
        }
        player.sendMessage(joinMessage);

        int lobbySize = plugin.getLobby().getPlayers().size();

        if (lobbySize == 0) {
            String neededPlayersMessage = ChatColor.translateAlternateColorCodes('&', config.getString("messages.needed-players"));
            player.sendMessage(neededPlayersMessage);
            for (Player player1 : plugin.getLobby().getPlayers()) {
                player1.sendMessage(neededPlayersMessage);
            }
        } else {
            String gameStartMessage;
            int startSeconds = (lobbySize >= 3) ? 10 : 25;
            gameStartMessage = ChatColor.translateAlternateColorCodes('&', config.getString("messages.game-start").replace("%seconds%", Integer.toString(startSeconds)));

            player.sendMessage(gameStartMessage);
            for (Player player1 : plugin.getLobby().getPlayers()) {
                player1.sendMessage(gameStartMessage);
            }

            if (plugin.getLobbyState() == LobbyState.IDLE) {
                plugin.setLobbyState(LobbyState.STARTING);
                lobbyPlayers.add(player.getUniqueId());
                plugin.getLobby().getPlayers().forEach(player1 -> lobbyPlayers.add(player1.getUniqueId()));

                gameTimer.start();
                gameTimer = new Timer(startSeconds, plugin);
                gameTimer.whenComplete(() -> PS3Minigames.INSTANCE.getGameUtil().startNewGame(lobbyPlayers, plugin.getServer().getWorld("cavern")));
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Game game = PS3Minigames.INSTANCE.getGameUtil().getGameByWorld(e.getPlayer().getWorld());
        if (game != null) {
            game.getPlayers().remove(e.getPlayer().getUniqueId());
            if (game.getPlayers().size() <= 1) {
                PS3Minigames.INSTANCE.getGameUtil().stopGame(game);
            }
        }
        lobbyPlayers.remove(e.getPlayer().getUniqueId());
    }
}
