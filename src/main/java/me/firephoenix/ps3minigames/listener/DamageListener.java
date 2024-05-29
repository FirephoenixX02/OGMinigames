package me.firephoenix.ps3minigames.listener;

import me.firephoenix.ps3minigames.PS3Minigames;
import me.firephoenix.ps3minigames.game.Game;
import me.firephoenix.ps3minigames.states.GameState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * @author NieGestorben
 * Copyright© (c) 2024, All Rights Reserved.
 */
public class DamageListener implements Listener {

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player)) return;

        Player player = (Player) e.getEntity();

        Game game = PS3Minigames.INSTANCE.getGameUtil().getGameByWorld(player.getWorld());

        if (game == null) return;

        GameState state = game.getGameState();

        if (state == GameState.INVINCIBILITY || state == GameState.STARTING) e.setCancelled(true);
    }

}
