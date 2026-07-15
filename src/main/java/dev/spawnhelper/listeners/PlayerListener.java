package dev.spawnhelper.listeners;

import dev.spawnhelper.SpawnConfig;
import dev.spawnhelper.SpawnHelperPlugin;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PlayerListener implements Listener {

    private final SpawnHelperPlugin plugin;

    // Stored per-player so we can restore on leave
    private final Map<UUID, GameMode> savedGamemodes  = new HashMap<>();
    private final Map<UUID, Float>    savedWalkSpeeds = new HashMap<>();
    private final Map<UUID, Float>    savedFlySpeeds  = new HashMap<>();
    private final Set<UUID> activeSpawnPlayers = new HashSet<>();

    public PlayerListener(SpawnHelperPlugin plugin) {
        this.plugin = plugin;
    }

    // ── Join / quit ───────────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        updateSpawnState(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getCollisionManager().removePlayer(player);
        activeSpawnPlayers.remove(player.getUniqueId());
        // Clean up stored state (don't restore — they disconnected)
        savedGamemodes.remove(player.getUniqueId());
        savedWalkSpeeds.remove(player.getUniqueId());
        savedFlySpeeds.remove(player.getUniqueId());
    }

    // ── World change ──────────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        updateSpawnState(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getTo() == null) return;
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) return;
        updateSpawnState(event.getPlayer());
    }

    // ── Item drop / pickup ────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getSpawnConfig().isInSpawnArea(player.getLocation())) return;
        if (player.hasPermission("spawnhelper.bypass")) return;
        if (plugin.getSpawnConfig().isNoItemDrop()) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPickupItem(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!plugin.getSpawnConfig().isInSpawnArea(player.getLocation())) return;
        if (player.hasPermission("spawnhelper.bypass")) return;
        if (plugin.getSpawnConfig().isNoItemPickup()) event.setCancelled(true);
    }

    // ── Entry / exit logic ────────────────────────────────────────────────────

    private void onEnterSpawn(Player player) {
        SpawnConfig cfg = plugin.getSpawnConfig();

        plugin.getCollisionManager().addPlayer(player);

        // Flight
        if (cfg.isAllowFlight() && !player.hasPermission("spawnhelper.bypass")) {
            player.setAllowFlight(true);
        }

        // Gamemode
        if (cfg.isGamemodeEnforced() && !player.hasPermission("spawnhelper.bypass")) {
            savedGamemodes.put(player.getUniqueId(), player.getGameMode());
            player.setGameMode(cfg.getEnforcedGamemode());
        }

        // Speed
        if (cfg.isSpeedEnabled() && !player.hasPermission("spawnhelper.bypass")) {
            savedWalkSpeeds.put(player.getUniqueId(), player.getWalkSpeed());
            savedFlySpeeds.put(player.getUniqueId(),  player.getFlySpeed());
            player.setWalkSpeed(cfg.getWalkSpeed());
            player.setFlySpeed(cfg.getFlySpeed());
        }

        // Greeting
        if (cfg.isGreetingEnabled()) {
            showGreeting(player, cfg);
        }
    }

    private void onLeaveSpawn(Player player) {
        SpawnConfig cfg = plugin.getSpawnConfig();
        UUID id = player.getUniqueId();

        plugin.getCollisionManager().removePlayer(player);

        // Revoke flight (only if not in creative/spectator)
        if (cfg.isAllowFlight() && !player.hasPermission("spawnhelper.bypass")) {
            if (player.getGameMode() != GameMode.CREATIVE && player.getGameMode() != GameMode.SPECTATOR) {
                player.setAllowFlight(false);
                player.setFlying(false);
            }
        }

        // Restore gamemode
        if (cfg.isGamemodeRestoreOnLeave()) {
            GameMode prev = savedGamemodes.remove(id);
            if (prev != null) player.setGameMode(prev);
        } else {
            savedGamemodes.remove(id);
        }

        // Restore speed
        Float walk = savedWalkSpeeds.remove(id);
        Float fly  = savedFlySpeeds.remove(id);
        if (walk != null) player.setWalkSpeed(walk);
        if (fly  != null) player.setFlySpeed(fly);
    }

    // ── Greeting ──────────────────────────────────────────────────────────────

    private static void showGreeting(Player player, SpawnConfig cfg) {
        LegacyComponentSerializer serial = LegacyComponentSerializer.legacyAmpersand();

        String titleStr    = cfg.getGreetingTitle();
        String subtitleStr = cfg.getGreetingSubtitle();
        String actionBar   = cfg.getGreetingActionBar();
        String chat        = cfg.getGreetingChat();

        // Title + subtitle
        if (!titleStr.isEmpty() || !subtitleStr.isEmpty()) {
            Title.Times times = Title.Times.times(
                    Duration.ofMillis(cfg.getGreetingFadeIn()  * 50L),
                    Duration.ofMillis(cfg.getGreetingStay()    * 50L),
                    Duration.ofMillis(cfg.getGreetingFadeOut() * 50L)
            );
            player.showTitle(Title.title(
                    serial.deserialize(titleStr),
                    serial.deserialize(subtitleStr),
                    times
            ));
        }

        // Action bar
        if (!actionBar.isEmpty()) {
            player.sendActionBar(serial.deserialize(actionBar));
        }

        // Chat message
        if (!chat.isEmpty()) {
            player.sendMessage(serial.deserialize(chat));
        }
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private void updateSpawnState(Player player) {
        boolean shouldBeActive = plugin.getSpawnConfig().isInSpawnArea(player.getLocation());
        boolean isActive = activeSpawnPlayers.contains(player.getUniqueId());

        if (shouldBeActive && !isActive) {
            onEnterSpawn(player);
            activeSpawnPlayers.add(player.getUniqueId());
        } else if (!shouldBeActive && isActive) {
            onLeaveSpawn(player);
            activeSpawnPlayers.remove(player.getUniqueId());
        }
    }
}
