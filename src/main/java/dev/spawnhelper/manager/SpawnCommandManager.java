package dev.spawnhelper.manager;

import dev.spawnhelper.SpawnConfig;
import dev.spawnhelper.SpawnHelperPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles all runtime logic for the /spawn command:
 * <ul>
 *   <li>Per-player cooldown tracking</li>
 *   <li>Countdown task with action-bar display</li>
 *   <li>Movement cancellation via {@link PlayerMoveEvent}</li>
 * </ul>
 */
public class SpawnCommandManager implements Listener {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();

    private final SpawnHelperPlugin plugin;

    /** Pending countdown tasks keyed by player UUID. */
    private final Map<UUID, BukkitTask> pending     = new HashMap<>();
    /** Cooldown expiry timestamps (epoch ms) keyed by player UUID. */
    private final Map<UUID, Long>       cooldowns   = new HashMap<>();

    public SpawnCommandManager(SpawnHelperPlugin plugin) {
        this.plugin = plugin;
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Entry point called by {@link dev.spawnhelper.command.SpawnCommand}.
     * Validates the command state, applies cooldown, then either teleports
     * instantly or starts a countdown.
     */
    public void startTeleport(Player player) {
        SpawnConfig cfg = plugin.getSpawnConfig();

        if (!cfg.isSpawnCommandEnabled()) {
            player.sendMessage(LEGACY.deserialize("&c[SpawnHelper] The /spawn command is currently disabled."));
            return;
        }

        if (pending.containsKey(player.getUniqueId())) {
            player.sendMessage(LEGACY.deserialize("&c[SpawnHelper] You already have a teleport pending!"));
            return;
        }

        // Cooldown check (bypass permission skips it)
        if (!player.hasPermission("spawnhelper.spawn.nocooldown")) {
            long remaining = remainingCooldownSeconds(player.getUniqueId());
            if (remaining > 0) {
                String msg = cfg.getSpawnCooldownMessage().replace("{time}", formatTime(remaining));
                player.sendMessage(LEGACY.deserialize(msg));
                return;
            }
        }

        Location dest = resolveDestination(cfg);
        if (dest == null) {
            player.sendMessage(LEGACY.deserialize(
                    "&c[SpawnHelper] Spawn world \"" + cfg.getSpawnWorld() + "\" not found!"));
            return;
        }

        int delay = cfg.getSpawnDelay();
        if (delay <= 0) {
            doTeleport(player, dest);
        } else {
            startCountdown(player, dest, delay);
        }
    }

    /** Clear a specific player's cooldown (admin use). */
    public void clearCooldown(UUID id) {
        cooldowns.remove(id);
    }

    /** Returns the remaining cooldown in seconds (0 if none). */
    public long remainingCooldownSeconds(UUID id) {
        Long expiry = cooldowns.get(id);
        if (expiry == null) return 0;
        long remaining = (expiry - System.currentTimeMillis() + 999) / 1000;
        return Math.max(0, remaining);
    }

    /** Cancel all pending teleports (called on plugin disable). */
    public void cleanup() {
        pending.values().forEach(t -> { if (!t.isCancelled()) t.cancel(); });
        pending.clear();
    }

    // ── Movement / quit listeners ─────────────────────────────────────────────

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        if (!pending.containsKey(id)) return;
        if (!plugin.getSpawnConfig().isSpawnCancelOnMove()) return;

        // Only react to actual position change — ignore pure head rotation
        Location from = event.getFrom(), to = event.getTo();
        if (from.getBlockX() == to.getBlockX()
                && from.getBlockY() == to.getBlockY()
                && from.getBlockZ() == to.getBlockZ()) return;

        Player player = event.getPlayer();
        player.sendMessage(LEGACY.deserialize(plugin.getSpawnConfig().getSpawnCancelledMessage()));
        player.sendActionBar(Component.text("✗ Teleport cancelled!", NamedTextColor.RED));
        cancelPending(id);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        cancelPending(event.getPlayer().getUniqueId());
    }

    // ── Internals ─────────────────────────────────────────────────────────────

    private void startCountdown(Player player, Location dest, int totalSeconds) {
        UUID id = player.getUniqueId();
        int[] remaining = {totalSeconds};

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!player.isOnline()) { cancelPending(id); return; }

            if (remaining[0] <= 0) {
                cancelPending(id);
                doTeleport(player, dest);
                return;
            }

            String template = plugin.getSpawnConfig()
                    .getSpawnTeleportingMessage()
                    .replace("{time}", String.valueOf(remaining[0]));
            player.sendActionBar(LEGACY.deserialize(template));
            remaining[0]--;

        }, 0L, 20L);

        pending.put(id, task);
    }

    private void doTeleport(Player player, Location dest) {
        UUID id = player.getUniqueId();

        player.teleport(dest);
        player.sendActionBar(Component.empty());
        player.sendMessage(LEGACY.deserialize(plugin.getSpawnConfig().getSpawnTeleportedMessage()));

        int cooldownSecs = plugin.getSpawnConfig().getSpawnCooldown();
        if (cooldownSecs > 0 && !player.hasPermission("spawnhelper.spawn.nocooldown")) {
            cooldowns.put(id, System.currentTimeMillis() + cooldownSecs * 1000L);
        }
    }

    private void cancelPending(UUID id) {
        BukkitTask task = pending.remove(id);
        if (task != null && !task.isCancelled()) task.cancel();
    }

    private Location resolveDestination(SpawnConfig cfg) {
        World world = Bukkit.getWorld(cfg.getSpawnWorld());
        if (world == null) return null;

        if (cfg.isSpawnUseWorldSpawn()) {
            return world.getSpawnLocation();
        }

        return new Location(world,
                cfg.getSpawnDestX(),
                cfg.getSpawnDestY(),
                cfg.getSpawnDestZ(),
                cfg.getSpawnDestYaw(),
                cfg.getSpawnDestPitch());
    }

    // ── Formatting ────────────────────────────────────────────────────────────

    static String formatTime(long seconds) {
        if (seconds < 60) return seconds + "s";
        long m = seconds / 60, s = seconds % 60;
        return s > 0 ? m + "m " + s + "s" : m + "m";
    }
}
