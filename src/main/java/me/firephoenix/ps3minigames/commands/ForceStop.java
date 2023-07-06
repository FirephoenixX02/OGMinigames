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
public class ForceStop implements CommandExecutor {

    public FileConfiguration config = PS3Minigames.INSTANCE.getConfig();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            int gameID;
            try {
                gameID = Integer.parseInt(args[0]);
            } catch (NumberFormatException exception) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("messages.invalid-game-id").replace("%id%", args[0])));
                return false;
            }
            if (PS3Minigames.INSTANCE.getGameUtil().getGameByID(gameID) == null || gameID == 0) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("messages.cant-find-game")));
                return true;
            } else {
                PS3Minigames.INSTANCE.getGameUtil().stopGame(Objects.requireNonNull(PS3Minigames.INSTANCE.getGameUtil().getGameByID(gameID)));
                return true;
            }
        }
        return false;
    }

}
