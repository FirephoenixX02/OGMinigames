package me.firephoenix.ps3minigames.util;

import me.firephoenix.ps3minigames.PS3Minigames;
import me.firephoenix.ps3minigames.game.Game;
import me.firephoenix.ps3minigames.states.GameState;
import org.bukkit.*;
import org.bukkit.block.Chest;
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


    public static int getRandomInRange(int min, int max) {
        Random random = new Random();
        return random.nextInt(max - min) + min;
    }

    public static float getRandomInRange(float min, float max) {
        return (float) (min + Math.random() * (max - min));
    }

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
                fillChests(newGame);
                timer.eachSecond(() -> {
                    for (UUID uuid : newGame.getPlayers()) {
                        if (Bukkit.getServer().getPlayer(uuid) == null) return;
                        Bukkit.getServer().getPlayer(uuid).sendTitle(ChatColor.translateAlternateColorCodes('&', "&6" + timer.getCounter()), "");
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
        if (game.getGameState() == GameState.STOPPING) {
            System.out.println("Someone tried to stop a game which is currently stopping, not possible, ignoring.");
            return;
        }
        game.setGameState(GameState.STOPPING);
        // Teleport all players + Clear inv + Reset Effects + Unfreeze
        game.getPlayers().forEach(uuid -> {
            Bukkit.getServer().getPlayer(uuid).teleport(new Location(PS3Minigames.INSTANCE.getServer().getWorld(PS3Minigames.INSTANCE.getConfig().getString("spawn-lobby.world")), PS3Minigames.INSTANCE.getConfig().getDouble("spawn-lobby.x"), PS3Minigames.INSTANCE.getConfig().getDouble("spawn-lobby.y"), PS3Minigames.INSTANCE.getConfig().getDouble("spawn-lobby.z"), (float) PS3Minigames.INSTANCE.getConfig().getDouble("spawn-lobby.yaw"), (float) PS3Minigames.INSTANCE.getConfig().getDouble("spawn-lobby.pitch")));
            Bukkit.getServer().getPlayer(uuid).getInventory().clear();
            Bukkit.getServer().getPlayer(uuid).getActivePotionEffects().clear();
            Bukkit.getServer().getPlayer(uuid).setHealth(20);
            Bukkit.getServer().getPlayer(uuid).setFoodLevel(20);
            Bukkit.getServer().getPlayer(uuid).getInventory().setBoots(null);
            Bukkit.getServer().getPlayer(uuid).getInventory().setLeggings(null);
            Bukkit.getServer().getPlayer(uuid).getInventory().setChestplate(null);
            Bukkit.getServer().getPlayer(uuid).getInventory().setHelmet(null);
            PS3Minigames.INSTANCE.getFrozenPlayer().remove(uuid);
        });
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

}
