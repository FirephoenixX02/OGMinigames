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
import org.bukkit.WorldCreator;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;

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

        String lobbyWorldName = getConfig().getString("spawn-lobby.world");

        lobby = Bukkit.getServer().getWorld(lobbyWorldName);

        //World doesn't exist but string in config is set, world is probably not loaded.
        if (lobby == null && getConfig().getString("spawn-lobby.world") != null) {
            //Try to load the world manually
            try {
              new WorldCreator(lobbyWorldName).createWorld();
            } catch (Exception e) {
                //World loading didn't work world folder doesn't exist/is corrupt.
                getLogger().log(Level.SEVERE, "There was an error loading the lobby world! The lobby name could be loaded from config but the world itself could not be found!");
            }
            getLogger().log(Level.INFO, "Loaded previously unloaded lobby world!");

            //Now that the world is loaded set global object again
            lobby = Bukkit.getServer().getWorld(lobbyWorldName);
        }

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
