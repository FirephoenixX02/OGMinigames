package me.firephoenix.ps3minigames;


import com.onarandombox.MultiverseCore.MultiverseCore;
import lombok.Getter;
import lombok.Setter;
import me.firephoenix.ps3minigames.commands.BuildMode;
import me.firephoenix.ps3minigames.commands.ForceStart;
import me.firephoenix.ps3minigames.commands.ForceStop;
import me.firephoenix.ps3minigames.commands.MapList;
import me.firephoenix.ps3minigames.game.Game;
import me.firephoenix.ps3minigames.listener.DeathListener;
import me.firephoenix.ps3minigames.listener.JoinQuitListener;
import me.firephoenix.ps3minigames.listener.MoveListener;
import me.firephoenix.ps3minigames.listener.WorldListener;
import me.firephoenix.ps3minigames.states.LobbyState;
import me.firephoenix.ps3minigames.util.GameUtil;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

@Getter
@Setter
public final class PS3Minigames extends JavaPlugin {

    public static PS3Minigames INSTANCE;

    public World lobby;

    public ArrayList<UUID> buildModePlayer = new ArrayList<>();

    public ArrayList<Game> games = new ArrayList<>();

    public HashMap<World, Game> worldToGameHashMap = new HashMap<>();

    public LobbyState lobbyState = LobbyState.IDLE;

    public MultiverseCore multiverseCore;

    public ArrayList<UUID> frozenPlayer = new ArrayList<>();

    public GameUtil gameUtil = new GameUtil();

    @Override
    public void onEnable() {
        // Set Instance
        INSTANCE = this;

        saveDefaultConfig();

        // Load Multiverse-API
        multiverseCore = (MultiverseCore) getServer().getPluginManager().getPlugin("Multiverse-Core");

        lobby = Bukkit.getServer().getWorld(getConfig().getString("spawn-lobby.world"));

        //Register Commands
        getCommand("build").setExecutor(new BuildMode());
        getCommand("forcestart").setExecutor(new ForceStart());
        getCommand("forcestop").setExecutor(new ForceStop());
        getCommand("maplist").setExecutor(new MapList());

        //Register Listener
        getServer().getPluginManager().registerEvents(new JoinQuitListener(), this);
        getServer().getPluginManager().registerEvents(new WorldListener(), this);
        getServer().getPluginManager().registerEvents(new MoveListener(), this);
        getServer().getPluginManager().registerEvents(new DeathListener(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
