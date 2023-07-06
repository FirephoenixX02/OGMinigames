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
import me.firephoenix.ps3minigames.states.GameState;
import me.firephoenix.ps3minigames.states.LobbyState;
import me.firephoenix.ps3minigames.util.Timer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

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

    public void startNewGame(ArrayList<UUID> players, World map) {
        String newName = map.getName() + "game" + getGames().size() + 1;
        boolean copydone = getMultiverseCore().getMVWorldManager().cloneWorld(map.getName(), newName);
        if (copydone) {
            boolean loadingdone = getMultiverseCore().getMVWorldManager().loadWorld(newName);
            if (loadingdone) {
                getMultiverseCore().getMVWorldManager().getMVWorld(newName).setAlias(newName);
                World gameWorld = Bukkit.getWorld(newName);
                Game newGame = new Game(getGames().size() + 1, players, gameWorld, GameState.STARTING);
                games.add(newGame);
                worldToGameHashMap.put(gameWorld, newGame);
                int spawnnumber = 1;
                for (UUID uuid : players) {
                    if (getServer().getPlayer(uuid) == null) return;
                    getServer().getPlayer(uuid).sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.teleporting")));
                    String configpathtospawnloc = "maps." + map.getName() + ".spawn" + spawnnumber++;
                    Location location = new Location(gameWorld, getConfig().getDouble(configpathtospawnloc + ".x"), getConfig().getDouble(configpathtospawnloc + ".y"), getConfig().getDouble(configpathtospawnloc + ".z"), (float) getConfig().getDouble(configpathtospawnloc + ".yaw"), (float) getConfig().getDouble(configpathtospawnloc + ".pitch"));
                    System.out.println(configpathtospawnloc + ".x");
                    Bukkit.getServer().getPlayer(uuid).teleport(location);
                    getFrozenPlayer().add(uuid);
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
                        frozenPlayer.remove(player.getUniqueId());
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.game-start-no-countdown")));
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
        game.getPlayers().forEach(uuid -> Bukkit.getServer().getPlayer(uuid).teleport(new Location(getServer().getWorld(getConfig().getString("spawn-lobby.world")), getConfig().getDouble("spawn-lobby.x"), getConfig().getDouble("spawn-lobby.y"), getConfig().getDouble("spawn-lobby.z"), (float) getConfig().getDouble("spawn-lobby.yaw"), (float) getConfig().getDouble("spawn-lobby.pitch"))));
        if (game.getMap().getPlayers().size() == 0) {
            getMultiverseCore().getMVWorldManager().deleteWorld(game.getMap().getName());
        }
        games.remove(game);
    }

    public Game getGameByWorld(World world) {
        return worldToGameHashMap.get(world);
    }

    @Nullable
    public Game getGameByID(int id) {
        //we use id - 1 because the id is the absolute size of the list which starts at 1 and the arraylist position starts at 0
        return getGames().stream().anyMatch(game -> game.getGameid() == id) ? getGames().get(id - 1) : null;
    }
}
