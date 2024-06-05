package me.firephoenix.ps3minigames.commands;

import me.firephoenix.ps3minigames.PS3Minigames;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Objects;


/**
 * @author NieGestorben
 * Copyright© (c) 2023, All Rights Reserved.
 */
public class MapList implements CommandExecutor {

 public FileConfiguration config = PS3Minigames.INSTANCE.getConfig();

 @Override
 public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
  if (args.length == 0) {
   StringBuilder sb = new StringBuilder();
   PS3Minigames.INSTANCE.getMultiverseCore().getMVWorldManager().getMVWorlds().stream().filter(world -> !world.getName().contains("world") && !world.getName().contains(config.getString("spawn-lobby.world"))).forEach(world -> sb.append(world.getName()).append(", "));
   sender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("messages.available-maps")));
   sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6" + sb));
   return true;
  }
  return false;
 }

}
