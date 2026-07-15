package dev.spawnhelper.listeners;

import dev.spawnhelper.SpawnHelperPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public class DamageListener implements Listener {

    private final SpawnHelperPlugin plugin;

    public DamageListener(SpawnHelperPlugin plugin) {
        this.plugin = plugin;
    }

    private boolean hasBypass(Player player) {
        return player.hasPermission("spawnhelper.bypass");
    }

    // ------------------------------------------------------------------
    // All damage to players
    // ------------------------------------------------------------------

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!plugin.getSpawnConfig().isInSpawnArea(player.getLocation())) return;
        if (hasBypass(player)) return;

        if (plugin.getSpawnConfig().isNoDamage()) {
            event.setCancelled(true);
        }
    }

    // ------------------------------------------------------------------
    // PvP (attacker is a player)
    // ------------------------------------------------------------------

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        if (!plugin.getSpawnConfig().isInSpawnArea(victim.getLocation())) return;
        if (hasBypass(victim)) return;

        if (event.getDamager() instanceof Player attacker) {
            // Check bypass on the attacker side too
            if (hasBypass(attacker)) return;
            if (plugin.getSpawnConfig().isNoPvp()) {
                event.setCancelled(true);
            }
        }
        // General damage is already caught by onEntityDamage above
    }

    // ------------------------------------------------------------------
    // Hunger
    // ------------------------------------------------------------------

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!plugin.getSpawnConfig().isInSpawnArea(player.getLocation())) return;
        if (hasBypass(player)) return;

        // Only freeze hunger loss, not healing (new level < current)
        if (plugin.getSpawnConfig().isNoHunger() && event.getFoodLevel() < player.getFoodLevel()) {
            event.setCancelled(true);
        }
    }
}
