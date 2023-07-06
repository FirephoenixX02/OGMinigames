package me.firephoenix.ps3minigames.listener;

import me.firephoenix.ps3minigames.PS3Minigames;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * @author NieGestorben
 * Copyright© (c) 2023, All Rights Reserved.
 */
public class MoveListener implements Listener {

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (PS3Minigames.INSTANCE.getFrozenPlayer().contains(e.getPlayer().getUniqueId()) && e.getFrom() != e.getTo()) {
            e.setTo(e.getFrom());
        }
    }
}
