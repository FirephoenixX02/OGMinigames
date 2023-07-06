package me.firephoenix.ps3minigames.util;

import me.firephoenix.ps3minigames.PS3Minigames;
import me.firephoenix.ps3minigames.game.Game;
import me.firephoenix.ps3minigames.states.GameState;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.UUID;

/**
 * @author NieGestorben
 * Copyright© (c) 2023, All Rights Reserved.
 */
public class GameUtil {


 public void startNewGame(ArrayList<UUID> players, World map) {
  int ID = PS3Minigames.INSTANCE.getGames().size() + 1;
  String newName = map.getName() + "game" + ID;
  boolean copydone = PS3Minigames.INSTANCE.getMultiverseCore().getMVWorldManager().cloneWorld(map.getName(), newName);
  if (copydone) {
   boolean loadingdone = PS3Minigames.INSTANCE.getMultiverseCore().getMVWorldManager().loadWorld(newName);
   if (loadingdone) {
    PS3Minigames.INSTANCE.getMultiverseCore().getMVWorldManager().getMVWorld(newName).setAlias(newName);
    World gameWorld = Bukkit.getWorld(newName);
    Game newGame = new Game(ID, players, gameWorld, GameState.STARTING);
    PS3Minigames.INSTANCE.getGames().add(newGame);
    PS3Minigames.INSTANCE.getWorldToGameHashMap().put(gameWorld, newGame);
    int spawnnumber = 1;
    for (UUID uuid : players) {
     if (PS3Minigames.INSTANCE.getServer().getPlayer(uuid) == null) return;
     PS3Minigames.INSTANCE.getServer().getPlayer(uuid).sendMessage(ChatColor.translateAlternateColorCodes('&', PS3Minigames.INSTANCE.getConfig().getString("messages.teleporting")));
     String configpathtospawnloc = "maps." + map.getName() + ".spawn" + spawnnumber++;
     Location location = new Location(gameWorld, PS3Minigames.INSTANCE.getConfig().getDouble(configpathtospawnloc + ".x"), PS3Minigames.INSTANCE.getConfig().getDouble(configpathtospawnloc + ".y"), PS3Minigames.INSTANCE.getConfig().getDouble(configpathtospawnloc + ".z"), (float) PS3Minigames.INSTANCE.getConfig().getDouble(configpathtospawnloc + ".yaw"), (float) PS3Minigames.INSTANCE.getConfig().getDouble(configpathtospawnloc + ".pitch"));
     Bukkit.getServer().getPlayer(uuid).teleport(location);
     PS3Minigames.INSTANCE.getFrozenPlayer().add(uuid);
    }
    Timer timer = new Timer(10, PS3Minigames.INSTANCE);
    timer.start();
    timer.eachSecond(() -> {
     for (UUID uuid : newGame.getPlayers()) {
      if (Bukkit.getServer().getPlayer(uuid) == null) return;
      Bukkit.getServer().getPlayer(uuid).sendTitle(ChatColor.translateAlternateColorCodes('&',"&6" + timer.getCounter()), "");
     }
    });
    timer.whenComplete(() -> {
     newGame.setGameState(GameState.RUNNING);
     gameWorld.getPlayers().forEach(player -> {
      PS3Minigames.INSTANCE.getFrozenPlayer().remove(player.getUniqueId());
      player.sendMessage(ChatColor.translateAlternateColorCodes('&', PS3Minigames.INSTANCE.getConfig().getString("messages.game-start-no-countdown")));
     });
    });
   } else {
    System.out.println("error while trying to load the world!");
   }
  } else {
   System.out.println("error while trying to copy the world!");
  }
 }

 public void stopGame(Game game) {
  if (game.getGameState() != GameState.STOPPING) game.setGameState(GameState.STOPPING);
  game.getPlayers().forEach(uuid -> Bukkit.getServer().getPlayer(uuid).teleport(new Location(PS3Minigames.INSTANCE.getServer().getWorld(PS3Minigames.INSTANCE.getConfig().getString("spawn-lobby.world")), PS3Minigames.INSTANCE.getConfig().getDouble("spawn-lobby.x"), PS3Minigames.INSTANCE.getConfig().getDouble("spawn-lobby.y"), PS3Minigames.INSTANCE.getConfig().getDouble("spawn-lobby.z"), (float) PS3Minigames.INSTANCE.getConfig().getDouble("spawn-lobby.yaw"), (float) PS3Minigames.INSTANCE.getConfig().getDouble("spawn-lobby.pitch"))));
  if (game.getMap().getPlayers().size() == 0) {
   PS3Minigames.INSTANCE.getMultiverseCore().getMVWorldManager().deleteWorld(game.getMap().getName());
  }
  PS3Minigames.INSTANCE.getGames().remove(game);
 }

 @Nullable
 public Game getGameByWorld(World world) {
  return PS3Minigames.INSTANCE.getWorldToGameHashMap().get(world);
 }

 @Nullable
 public Game getGameByID(int id) {
  //we use id - 1 because the id is the absolute size of the list which starts at 1 and the arraylist position starts at 0
  return PS3Minigames.INSTANCE.getGames().stream().anyMatch(game -> game.getGameid() == id) ? PS3Minigames.INSTANCE.getGames().get(id - 1) : null;
 }
 
}
