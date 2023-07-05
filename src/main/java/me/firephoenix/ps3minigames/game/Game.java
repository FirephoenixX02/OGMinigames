package me.firephoenix.ps3minigames.game;

import lombok.Getter;
import lombok.Setter;
import me.firephoenix.ps3minigames.states.GameState;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.UUID;

/**
 * @author NieGestorben
 * Copyright© (c) 2023, All Rights Reserved.
 */

@Getter
@Setter
public class Game {

    public int gameid;

    public ArrayList<UUID> players;

    public GameState gameState;

    public World map;

    public Game(int gameid, ArrayList<UUID> players, World map, GameState gameState) {
        this.gameid = gameid;
        this.players = players;
        this.gameState = gameState;
        this.map = map;
    }

}
