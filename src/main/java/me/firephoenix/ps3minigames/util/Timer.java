package me.firephoenix.ps3minigames.util;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

/**
 * @author tiiita_
 * Created on Dezember 17, 2022 | 18:21:40
 * (●'◡'●)
 */
public class Timer {
    private final int duration;
    private final Plugin plugin;
    @Getter
    int counter;
    BukkitTask bukkitTask;
    private boolean running = false;
    private Runnable whenComplete;
    private Runnable eachSecond;
    public Timer(int durationInSeconds, Plugin plugin) {
        this.duration = durationInSeconds;
        this.plugin = plugin;
    }

    public void start() {
        counter = duration;
        running = true;
        bukkitTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (counter > 0) {
                if (eachSecond != null) {
                    eachSecond.run();
                }
                counter--;
            } else {
                bukkitTask.cancel();
                running = false;

                if (whenComplete != null) {
                    whenComplete.run();
                }
            }

        }, 0, 20);

    }

    public void stop() {
        bukkitTask.cancel();
        bukkitTask = null;
        counter = duration;
        running = false;
    }

    public void whenComplete(Runnable runnable) {
        this.whenComplete = runnable;
    }

    public void eachSecond(Runnable runnable) {
        this.eachSecond = runnable;
    }

    public boolean isRunning() {
        return running;
    }

}