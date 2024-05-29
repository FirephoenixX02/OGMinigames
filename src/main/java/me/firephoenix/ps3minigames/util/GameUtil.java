package me.firephoenix.ps3minigames.util;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import me.firephoenix.ps3minigames.PS3Minigames;
import me.firephoenix.ps3minigames.game.Game;
import me.firephoenix.ps3minigames.states.GameState;
import org.bukkit.*;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * @author NieGestorben
 * Copyright© (c) 2023, All Rights Reserved.
 */
public class GameUtil {

    public void startNewGame(ArrayList<UUID> players, World map) {
        PS3Minigames plugin = PS3Minigames.INSTANCE;
        MultiverseCore mvCore = plugin.getMultiverseCore();
        MVWorldManager worldManager = mvCore.getMVWorldManager();

        int gameId = plugin.getGames().size() + 1;
        String newWorldName = map.getName() + "game" + gameId;

        if (!worldManager.cloneWorld(map.getName(), newWorldName)) {
            System.out.println("Error while trying to copy the world!");
            return;
        }

        if (!worldManager.loadWorld(newWorldName)) {
            System.out.println("Error while trying to load the world!");
            return;
        }

        MultiverseWorld mvWorld = worldManager.getMVWorld(newWorldName);
        mvWorld.setAlias(newWorldName);
        World gameWorld = Bukkit.getWorld(newWorldName);

        Game newGame = new Game(gameId, players, gameWorld, GameState.STARTING);
        plugin.getGames().add(newGame);
        plugin.getWorldToGameHashMap().put(gameWorld, newGame);

        int spawnNumber = 1;
        for (UUID uuid : players) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) return;

            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.teleporting")));

            String spawnPath = "maps." + map.getName() + ".spawn" + spawnNumber++;
            Location spawnLocation = new Location(
                    gameWorld,
                    plugin.getConfig().getDouble(spawnPath + ".x"),
                    plugin.getConfig().getDouble(spawnPath + ".y"),
                    plugin.getConfig().getDouble(spawnPath + ".z"),
                    (float) plugin.getConfig().getDouble(spawnPath + ".yaw"),
                    (float) plugin.getConfig().getDouble(spawnPath + ".pitch")
            );

            player.teleport(spawnLocation);
            plugin.getFrozenPlayer().add(uuid);
        }

        startCountdown(newGame, 10, "&6");
        startInvincibilityTimer(newGame, 25, "&6Invulnerability wears off in ");
    }

    public void stopGame(Game game) {
        if (game.getGameState() == GameState.STOPPING) {
            System.out.println("Someone tried to stop a game which is currently stopping, not possible, ignoring.");
            return;
        }

        List<UUID> players = game.getPlayers();
        Player winner = Bukkit.getPlayer(players.get(0));

        if (winner != null) {
            String gameWonMessage = ChatColor.translateAlternateColorCodes('&',
                    PS3Minigames.INSTANCE.getConfig().getString("messages.game-won").replace("%winner%", winner.getDisplayName()));

            for (UUID uuid : players) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    player.sendMessage(gameWonMessage);
                }
            }
        }

        game.setGameState(GameState.STOPPING);

        Location spawnLocation = new Location(
                PS3Minigames.INSTANCE.getServer().getWorld(PS3Minigames.INSTANCE.getConfig().getString("spawn-lobby.world")),
                PS3Minigames.INSTANCE.getConfig().getDouble("spawn-lobby.x"),
                PS3Minigames.INSTANCE.getConfig().getDouble("spawn-lobby.y"),
                PS3Minigames.INSTANCE.getConfig().getDouble("spawn-lobby.z"),
                (float) PS3Minigames.INSTANCE.getConfig().getDouble("spawn-lobby.yaw"),
                (float) PS3Minigames.INSTANCE.getConfig().getDouble("spawn-lobby.pitch")
        );

        for (UUID uuid : players) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                player.teleport(spawnLocation);
                resetInventory(player);
            }
        }

        if (game.getMap().getPlayers().isEmpty()) {
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

    public void fillChests(Game game) {
        for (Chunk chunk : game.getMap().getLoadedChunks()) {
            Arrays.stream(chunk.getTileEntities()).filter(tileentity -> tileentity instanceof Chest).forEach((chest) -> {
                Chest chestblock = (Chest) chest;
                fillChest(chestblock);
            });
        }
    }

    public void fillChest(Chest chest) {
        int slots = getRandomInRange(4, chest.getBlockInventory().getSize() / 3);
        for (int i = 0; i < slots; i++) {
            int randomSlot = getRandomInRange(0, slots);
            Random random = new Random();
            int chance = random.nextInt(10);
            boolean potion = (chance == 1 || chance == 2);
            if (potion) {
                ItemStack[] possiblePotions = getPossiblePotionsFromConfig("chest-loot-potions").toArray(new ItemStack[0]);
                ItemStack randomPotion = possiblePotions[random.nextInt((int) Arrays.stream(possiblePotions).count())];
                chest.getBlockInventory().setItem(randomSlot, randomPotion);
            } else {
                Material[] possibleItems = getPossibleMaterialsFromConfig("chest-loot").keySet().toArray(new Material[0]);
                Material randomItem = possibleItems[random.nextInt((int) Arrays.stream(possibleItems).count())];
                int itemAmount = getPossibleMaterialsFromConfig("chest-loot").get(randomItem);
                ItemStack item = new ItemBuilder(randomItem, itemAmount).toItemStack();
                chest.getBlockInventory().setItem(randomSlot, item);
            }
        }
    }

    private Map<Material, Integer> getPossibleMaterialsFromConfig(String path) {
        Collection<String> entries = PS3Minigames.INSTANCE.getConfig().getStringList(path);
        if (entries == null) throw new IllegalArgumentException("Cannot find path: " + path + "in config.yml!");

        Map<Material, Integer> materialToMaxStackSize = new HashMap<>();

        for (String entry : entries) {
            String[] parts = entry.split("#", 2);
            String materialName = parts[0];
            materialName = materialName.replaceAll("\\d+$", "");
            int maxStackSize = Integer.parseInt(parts[1]);

            Material material = Material.getMaterial(materialName);
            if (material == null)
                throw new IllegalArgumentException("Material with the name: " + materialName + " was not found!");

            materialToMaxStackSize.put(material, maxStackSize);
        }

        return materialToMaxStackSize;
    }

    private ArrayList<ItemStack> getPossiblePotionsFromConfig(String path) {
        Collection<Integer> entries = PS3Minigames.INSTANCE.getConfig().getIntegerList(path);
        if (entries == null) throw new IllegalArgumentException("Cannot find path: " + path + "in config.yml!");

        ArrayList<ItemStack> potionToMaxStackSize = new ArrayList<>();

        for (Integer entry : entries) {
            int potionID = entry;

            ItemStack potion = new ItemStack(Material.POTION, 1, (short) potionID);

            potionToMaxStackSize.add(potion);
        }

        return potionToMaxStackSize;
    }

    public double roundTo(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(Double.toString(value));
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    private void startCountdown(Game game, int seconds, String titleColor) {
        Timer timer = new Timer(seconds, PS3Minigames.INSTANCE);
        timer.start();

        timer.eachSecond(() -> {
            for (UUID uuid : game.getPlayers()) {
                Player player = Bukkit.getPlayer(uuid);
                if (player == null) return;
                player.sendTitle(ChatColor.translateAlternateColorCodes('&', titleColor + timer.getCounter()), "");
            }
        });

        timer.whenComplete(() -> {
            game.setGameState(GameState.INVINCIBILITY);
            game.getMap().getPlayers().forEach(player -> {
                PS3Minigames.INSTANCE.getFrozenPlayer().remove(player.getUniqueId());
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', PS3Minigames.INSTANCE.getConfig().getString("messages.game-start-no-countdown")));
            });
        });

        fillChests(game);
    }

    private void startInvincibilityTimer(Game game, int seconds, String messagePrefix) {
        Timer invincibilityTimer = new Timer(seconds, PS3Minigames.INSTANCE);
        invincibilityTimer.start();

        invincibilityTimer.eachSecond(() -> {
            for (UUID uuid : game.getPlayers()) {
                Player player = Bukkit.getPlayer(uuid);
                if (player == null) return;
                player.sendTitle("", ChatColor.translateAlternateColorCodes('&', messagePrefix + invincibilityTimer.getCounter()));
            }
        });

        invincibilityTimer.whenComplete(() -> {
            game.setGameState(GameState.RUNNING);
        });
    }

    public static int getRandomInRange(int min, int max) {
        Random random = new Random();
        return random.nextInt(max - min) + min;
    }

    public static float getRandomInRange(float min, float max) {
        return (float) (min + Math.random() * (max - min));
    }

    public static void resetInventory(Player player) {
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
        player.setHealth(20);
        player.setFoodLevel(20);
        player.getInventory().clear();
        player.getInventory().setBoots(null);
        player.getInventory().setLeggings(null);
        player.getInventory().setChestplate(null);
        player.getInventory().setHelmet(null);
        player.setGameMode(GameMode.SURVIVAL);
    }

}
