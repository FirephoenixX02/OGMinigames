package me.firephoenix.ps3minigames.listener;

import me.firephoenix.ps3minigames.PS3Minigames;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

/**
 * @author NieGestorben
 * Copyright© (c) 2023, All Rights Reserved.
 */
public class WorldListener implements Listener {

    public FileConfiguration config = PS3Minigames.INSTANCE.getConfig();

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if (!PS3Minigames.INSTANCE.getBuildModePlayer().contains(e.getPlayer().getUniqueId())) {
            e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("messages.block-place-forbidden")));
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (!PS3Minigames.INSTANCE.getBuildModePlayer().contains(e.getPlayer().getUniqueId())) {
            e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("messages.block-break-forbidden")));
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onAttack(PlayerInteractAtEntityEvent e) {
        if (e.getPlayer().getWorld().equals(PS3Minigames.INSTANCE.getLobby())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDestroyCrops(PlayerInteractEvent e) {
        if (e.getClickedBlock() != null) {
            if (e.getClickedBlock().getType() == Material.AIR) {
                e.setCancelled(true);
                return;
            }
        }
        if (!PS3Minigames.INSTANCE.getBuildModePlayer().contains(e.getPlayer().getUniqueId())) {
            if (e.getClickedBlock() == null) return;
            Block clickedBlock = e.getClickedBlock();

            //Left or Right click?
            if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
                //Crops?
                if (clickedBlock.getType() == Material.CROPS) {
                    e.setCancelled(true);
                }
            }
        }
    }


    @EventHandler
    public void onItemPickUp(PlayerPickupItemEvent e) {
        if (!PS3Minigames.INSTANCE.getBuildModePlayer().contains(e.getPlayer().getUniqueId())) {
            if (e.getPlayer().getWorld().equals(PS3Minigames.INSTANCE.getLobby())) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        if (!PS3Minigames.INSTANCE.getBuildModePlayer().contains(e.getPlayer().getUniqueId())) {
            if (e.getPlayer().getWorld().equals(PS3Minigames.INSTANCE.getLobby())) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerDoorOpen(PlayerInteractEvent e) {
        if (e.getClickedBlock() != null) {
            if (e.getClickedBlock().getType() == Material.AIR) {
                e.setCancelled(true);
                return;
            }
        }
        if (!PS3Minigames.INSTANCE.getBuildModePlayer().contains(e.getPlayer().getUniqueId())) {
            if (e.getClickedBlock() == null) return;
            Block clickedBlock = e.getClickedBlock();

            //Left or Right click?
            if (e.getAction() == Action.LEFT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                //Door Block?
                if (clickedBlock.getType().name().endsWith("_DOOR")
                        || clickedBlock.getType().name().endsWith("_FENCE")
                        || clickedBlock.getType().name().endsWith("_GATE")) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onItemMove(InventoryClickEvent e) {
        if (!PS3Minigames.INSTANCE.getBuildModePlayer().contains(e.getWhoClicked().getUniqueId())) {
            if (e.getWhoClicked().getWorld().equals(PS3Minigames.INSTANCE.getLobby())) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            if (e.getEntity().getWorld().equals(PS3Minigames.INSTANCE.getLobby())) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onDamageByEntity(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player) {
            if (e.getDamager().getWorld().equals(PS3Minigames.INSTANCE.getLobby())) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent e) {
        if (e.getEntity() instanceof Player) {
            if (e.getEntity().getWorld().equals(PS3Minigames.INSTANCE.getLobby())) {
                e.setCancelled(true);
                Player player = (Player) e.getEntity();
                if (!(player.getFoodLevel() == 20)) player.setFoodLevel(20);
            }
        }
    }

    @EventHandler
    public void onBlockSpread(BlockSpreadEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onBlockGrowth(BlockGrowEvent e) {
        e.setCancelled(true);
    }

}
