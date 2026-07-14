package dev.spawnhelper.listeners;

import dev.spawnhelper.SpawnHelperPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.List;

public class CommandListener implements Listener {

    private final SpawnHelperPlugin plugin;

    public CommandListener(SpawnHelperPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (!player.getWorld().getName().equals(plugin.getSpawnConfig().getSpawnWorld())) return;
        if (player.hasPermission("spawnhelper.bypass")) return;
        if (!plugin.getSpawnConfig().isCommandBlacklistEnabled()) return;

        // Extract the base command (no leading slash, no arguments, no namespace)
        String raw = event.getMessage().toLowerCase().substring(1); // strip /
        String base = raw.split(" ")[0]; // e.g. "minecraft:kill" or "kill"
        String local = base.contains(":") ? base.split(":")[1] : base;

        List<String> blacklist = plugin.getSpawnConfig().getBlacklistedCommands();
        for (String blocked : blacklist) {
            String b = blocked.toLowerCase();
            if (local.equals(b) || base.equals(b)) {
                event.setCancelled(true);
                player.sendMessage("§c[SpawnHelper] That command cannot be used in spawn.");
                return;
            }
        }
    }
}
