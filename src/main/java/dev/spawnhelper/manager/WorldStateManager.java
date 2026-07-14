package dev.spawnhelper.manager;

import dev.spawnhelper.SpawnConfig;
import dev.spawnhelper.SpawnHelperPlugin;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitTask;

/**
 * Periodically enforces time-lock and weather-lock on the spawn world.
 *
 * <p>Time is set every 20 ticks (1 s) so the sun appears frozen.
 * Weather is cleared every 200 ticks (10 s) — enough to catch any reset.</p>
 */
public class WorldStateManager {

    private final SpawnHelperPlugin plugin;
    private BukkitTask task;

    public WorldStateManager(SpawnHelperPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        stop();
        if (!needsTask()) return;
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 20L, 20L);
    }

    public void stop() {
        if (task != null && !task.isCancelled()) {
            task.cancel();
            task = null;
        }
    }

    /** Call after toggling time-lock or weather-lock in the GUI. */
    public void restart() {
        start();
    }

    private boolean needsTask() {
        SpawnConfig cfg = plugin.getSpawnConfig();
        return cfg.isTimeLockEnabled() || cfg.isWeatherLock();
    }

    private void tick() {
        World world = Bukkit.getWorld(plugin.getSpawnConfig().getSpawnWorld());
        if (world == null) return;

        SpawnConfig cfg = plugin.getSpawnConfig();

        if (cfg.isTimeLockEnabled()) {
            world.setTime(cfg.getLockedTime());
        }

        if (cfg.isWeatherLock()) {
            if (world.hasStorm()) world.setStorm(false);
            if (world.isThundering()) world.setThundering(false);
        }
    }
}
