package dev.spawnhelper.listeners;

import dev.spawnhelper.SpawnHelperPlugin;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;

public class WorldListener implements Listener {

    private final SpawnHelperPlugin plugin;

    public WorldListener(SpawnHelperPlugin plugin) {
        this.plugin = plugin;
    }

    private boolean inSpawn(World world) {
        return world.getName().equals(plugin.getSpawnConfig().getSpawnWorld());
    }

    // ------------------------------------------------------------------
    // Mob spawning
    // ------------------------------------------------------------------

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (!inSpawn(event.getEntity().getWorld())) return;
        if (!plugin.getSpawnConfig().isNoMobSpawning()) return;

        // Only suppress natural/ambient spawns — allow command/plugin spawns
        CreatureSpawnEvent.SpawnReason reason = event.getSpawnReason();
        if (reason == CreatureSpawnEvent.SpawnReason.NATURAL
                || reason == CreatureSpawnEvent.SpawnReason.PATROL
                || reason == CreatureSpawnEvent.SpawnReason.RAID
                || reason == CreatureSpawnEvent.SpawnReason.VILLAGE_INVASION
                || reason == CreatureSpawnEvent.SpawnReason.REINFORCEMENTS
                || reason == CreatureSpawnEvent.SpawnReason.JOCKEY
                || reason == CreatureSpawnEvent.SpawnReason.DEFAULT) {
            event.setCancelled(true);
        }
    }

    // ------------------------------------------------------------------
    // Explosions
    // ------------------------------------------------------------------

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        if (!inSpawn(event.getEntity().getWorld())) return;
        if (plugin.getSpawnConfig().isNoExplosionDamage()) {
            // Clear block damage list but keep the explosion visual/sound
            event.blockList().clear();
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onExplosionPrime(ExplosionPrimeEvent event) {
        if (!inSpawn(event.getEntity().getWorld())) return;
        // Cancel the explosion entirely if no-damage is on
        if (plugin.getSpawnConfig().isNoDamage()) {
            event.setCancelled(true);
        }
    }
}
