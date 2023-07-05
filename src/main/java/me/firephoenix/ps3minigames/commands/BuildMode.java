package me.firephoenix.ps3minigames.commands;

import me.firephoenix.ps3minigames.PS3Minigames;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

/**
 * @author NieGestorben
 * Copyright© (c) 2023, All Rights Reserved.
 */
public class BuildMode implements CommandExecutor {

    public FileConfiguration config = PS3Minigames.INSTANCE.getConfig();

    public PS3Minigames plugin = PS3Minigames.INSTANCE;
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }
        Player player = (Player) sender;

        if (!player.hasPermission(command.getPermission())) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("messages.no-permission")));
            return true;
        }

        if (args.length == 0) {
            if (plugin.getBuildModePlayer().contains(player.getUniqueId())) {
                plugin.getBuildModePlayer().remove(player.getUniqueId());
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("messages.build-mode-disabled")));
                return true;
            } else {
                plugin.getBuildModePlayer().add(player.getUniqueId());
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("messages.build-mode-enabled")));
                return true;
            }
        } else if (args.length == 1) {
            Player target = Bukkit.getPlayer(args[0]);
            if (target != null) {
                if (plugin.getBuildModePlayer().contains(player.getUniqueId())) {
                    plugin.getBuildModePlayer().remove(target.getUniqueId());
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("build-mode-disabled-for-player")));
                    return true;
                } else {
                    plugin.getBuildModePlayer().add(target.getUniqueId());
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("build-mode-enabled-for-player")));
                    return true;
                }
            } else {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("couldnt-find-player")));
            }
        } else {
            player.sendMessage("§7Please enter in a player name or leave the arguments empty!");
        }
        return false;
    }

}
