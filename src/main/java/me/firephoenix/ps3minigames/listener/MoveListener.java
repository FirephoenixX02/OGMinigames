package me.firephoenix.ps3minigames.listener;

import me.firephoenix.ps3minigames.PS3Minigames;
import me.firephoenix.ps3minigames.game.Game;
import me.firephoenix.ps3minigames.states.GameState;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Objects;

/**
 * @author NieGestorben
 * Copyright© (c) 2023, All Rights Reserved.
 */
public class MoveListener implements Listener {

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        // TODO: Make this less performance intensive. Currently the required performance increases exponentially.
        for (Game game : PS3Minigames.INSTANCE.getGames()) {
            if (game.getPlayers().stream().anyMatch(uuid -> e.getPlayer().getUniqueId().equals(uuid))) {
                if (game.getGameState() == GameState.STARTING) {
                    e.setTo(e.getFrom());
                }
            }
        }
    }
}
