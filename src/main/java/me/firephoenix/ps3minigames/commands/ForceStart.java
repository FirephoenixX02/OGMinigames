package me.firephoenix.ps3minigames.commands;

import me.firephoenix.ps3minigames.PS3Minigames;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.UUID;

/**
 * @author NieGestorben
 * Copyright© (c) 2023, All Rights Reserved.
 */
public class ForceStart implements CommandExecutor {

    public FileConfiguration config = PS3Minigames.INSTANCE.getConfig();

    public PS3Minigames plugin = PS3Minigames.INSTANCE;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            World world = Bukkit.getWorld(args[0]);
            if (world != null) {
                ArrayList<UUID> players = new ArrayList<>();
                PS3Minigames.INSTANCE.getLobby().getPlayers().forEach(player -> players.add(player.getUniqueId()));
                PS3Minigames.INSTANCE.startNewGame(players, world);
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("messages.force-starting-game").replace("%map%", world.getName())));
                return true;
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("messages.world-not-found")));
                return true;
            }
        }
        return false;
    }

}
