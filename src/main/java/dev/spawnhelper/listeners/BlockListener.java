package dev.spawnhelper.listeners;

import dev.spawnhelper.SpawnHelperPlugin;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class BlockListener implements Listener {

    private final SpawnHelperPlugin plugin;

    public BlockListener(SpawnHelperPlugin plugin) {
        this.plugin = plugin;
    }

    private boolean inSpawn(World world) {
        return world.getName().equals(plugin.getSpawnConfig().getSpawnWorld());
    }

    // ------------------------------------------------------------------
    // Breaking
    // ------------------------------------------------------------------

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!inSpawn(event.getBlock().getWorld())) return;
        if (event.getPlayer().hasPermission("spawnhelper.bypass")) return;

        if (plugin.getSpawnConfig().isNoBlockBreaking()) {
            event.setCancelled(true);
        }
    }

    // ------------------------------------------------------------------
    // Placing
    // ------------------------------------------------------------------

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!inSpawn(event.getBlock().getWorld())) return;
        if (event.getPlayer().hasPermission("spawnhelper.bypass")) return;

        if (plugin.getSpawnConfig().isNoBlockPlacing()) {
            event.setCancelled(true);
        }
    }

    // ------------------------------------------------------------------
    // Interaction (right-click on blocks)
    // ------------------------------------------------------------------

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getClickedBlock() == null) return;
        if (!inSpawn(event.getClickedBlock().getWorld())) return;
        if (player.hasPermission("spawnhelper.bypass")) return;

        // If interaction is explicitly disabled, cancel
        if (!plugin.getSpawnConfig().isAllowBlockInteraction()) {
            event.setCancelled(true);
        }
        // If interaction is allowed, do nothing — let the event pass through.
    }

    // ------------------------------------------------------------------
    // Fire spread
    // ------------------------------------------------------------------

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {
        if (!inSpawn(event.getBlock().getWorld())) return;
        if (plugin.getSpawnConfig().isNoFireSpread()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockSpread(BlockSpreadEvent event) {
        if (!inSpawn(event.getBlock().getWorld())) return;
        if (plugin.getSpawnConfig().isNoFireSpread()
                && event.getNewState().getType() == Material.FIRE) {
            event.setCancelled(true);
        }
    }
}
