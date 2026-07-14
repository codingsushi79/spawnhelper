package dev.spawnhelper.gui;

import dev.spawnhelper.SpawnConfig;
import dev.spawnhelper.SpawnHelperPlugin;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

public class GuiListener implements Listener {

    private static final long[] TIME_PRESETS = {0L, 6000L, 12000L, 18000L};
    private static final GameMode[] GAMEMODES = {
            GameMode.SURVIVAL, GameMode.ADVENTURE, GameMode.CREATIVE, GameMode.SPECTATOR
    };

    private final SpawnHelperPlugin plugin;

    public GuiListener(SpawnHelperPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof ConfigGui gui)) return;
        event.setCancelled(true);

        // Ignore clicks in the player's own inventory pane
        if (event.getClickedInventory() == null) return;
        if (!event.getClickedInventory().equals(event.getInventory())) return;

        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();
        ClickType click = event.getClick();

        if (slot == ConfigGui.SLOT_CLOSE) {
            player.closeInventory();
            return;
        }

        if (handleClick(slot, click)) {
            plugin.saveConfig();
            gui.refresh();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.2f);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof ConfigGui) {
            event.setCancelled(true);
        }
    }

    // ── Click dispatch ────────────────────────────────────────────────────────

    private boolean handleClick(int slot, ClickType click) {
        SpawnConfig cfg = plugin.getSpawnConfig();

        return switch (slot) {
            case ConfigGui.SLOT_NO_DAMAGE -> {
                cfg.set("settings.no-damage", !cfg.isNoDamage());
                yield true;
            }
            case ConfigGui.SLOT_NO_PVP -> {
                cfg.set("settings.no-pvp", !cfg.isNoPvp());
                yield true;
            }
            case ConfigGui.SLOT_NO_BREAK -> {
                cfg.set("settings.no-block-breaking", !cfg.isNoBlockBreaking());
                yield true;
            }
            case ConfigGui.SLOT_NO_PLACE -> {
                cfg.set("settings.no-block-placing", !cfg.isNoBlockPlacing());
                yield true;
            }
            case ConfigGui.SLOT_ALLOW_INTERACT -> {
                cfg.set("settings.allow-block-interaction", !cfg.isAllowBlockInteraction());
                yield true;
            }
            case ConfigGui.SLOT_NO_COLLISION -> {
                cfg.set("settings.no-collision", !cfg.isNoCollision());
                plugin.getCollisionManager().reapply();
                yield true;
            }
            case ConfigGui.SLOT_NO_HUNGER -> {
                cfg.set("settings.no-hunger", !cfg.isNoHunger());
                yield true;
            }
            case ConfigGui.SLOT_NO_ITEM_DROP -> {
                cfg.set("settings.no-item-drop", !cfg.isNoItemDrop());
                yield true;
            }
            case ConfigGui.SLOT_NO_ITEM_PICKUP -> {
                cfg.set("settings.no-item-pickup", !cfg.isNoItemPickup());
                yield true;
            }
            case ConfigGui.SLOT_ALLOW_FLIGHT -> {
                boolean newFlight = !cfg.isAllowFlight();
                cfg.set("settings.allow-flight", newFlight);
                applyFlightToSpawnPlayers(newFlight);
                yield true;
            }
            case ConfigGui.SLOT_NO_MOB_SPAWN -> {
                cfg.set("settings.no-mob-spawning", !cfg.isNoMobSpawning());
                yield true;
            }
            case ConfigGui.SLOT_NO_FIRE_SPREAD -> {
                cfg.set("settings.no-fire-spread", !cfg.isNoFireSpread());
                yield true;
            }
            case ConfigGui.SLOT_NO_EXPLOSION -> {
                cfg.set("settings.no-explosion-damage", !cfg.isNoExplosionDamage());
                yield true;
            }
            case ConfigGui.SLOT_TIME_LOCK -> {
                if (click.isLeftClick()) {
                    cfg.set("settings.time-lock.enabled", !cfg.isTimeLockEnabled());
                    plugin.getWorldStateManager().restart();
                } else if (click.isRightClick()) {
                    cfg.set("settings.time-lock.time", nextTimePreset(cfg.getLockedTime()));
                }
                yield true;
            }
            case ConfigGui.SLOT_WEATHER_LOCK -> {
                cfg.set("settings.weather-lock", !cfg.isWeatherLock());
                plugin.getWorldStateManager().restart();
                yield true;
            }
            case ConfigGui.SLOT_GAMEMODE -> {
                if (click.isLeftClick()) {
                    boolean newGm = !cfg.isGamemodeEnforced();
                    cfg.set("settings.gamemode-enforcement.enabled", newGm);
                    if (newGm) applyGamemodeToSpawnPlayers(cfg.getEnforcedGamemode());
                } else if (click.isRightClick()) {
                    GameMode next = nextGamemode(cfg.getEnforcedGamemode());
                    cfg.set("settings.gamemode-enforcement.gamemode", next.name());
                    if (cfg.isGamemodeEnforced()) applyGamemodeToSpawnPlayers(next);
                }
                yield true;
            }
            case ConfigGui.SLOT_SPEED -> {
                boolean newSpeed = !cfg.isSpeedEnabled();
                cfg.set("settings.speed.enabled", newSpeed);
                applySpeedToSpawnPlayers(newSpeed);
                yield true;
            }
            case ConfigGui.SLOT_GREETING -> {
                cfg.set("greeting.enabled", !cfg.isGreetingEnabled());
                yield true;
            }
            case ConfigGui.SLOT_CMD_BLACKLIST -> {
                cfg.set("settings.command-blacklist.enabled", !cfg.isCommandBlacklistEnabled());
                yield true;
            }
            case ConfigGui.SLOT_SPAWN_CMD -> {
                cfg.set("spawn-command.enabled", !cfg.isSpawnCommandEnabled());
                yield true;
            }
            default -> false;
        };
    }

    // ── Live-apply helpers ────────────────────────────────────────────────────

    private void applyFlightToSpawnPlayers(boolean allow) {
        String world = plugin.getSpawnConfig().getSpawnWorld();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.getWorld().getName().equals(world)) continue;
            if (p.hasPermission("spawnhelper.bypass")) continue;
            if (allow) {
                p.setAllowFlight(true);
            } else if (p.getGameMode() != GameMode.CREATIVE && p.getGameMode() != GameMode.SPECTATOR) {
                p.setAllowFlight(false);
                p.setFlying(false);
            }
        }
    }

    private void applyGamemodeToSpawnPlayers(GameMode gm) {
        String world = plugin.getSpawnConfig().getSpawnWorld();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.getWorld().getName().equals(world)) continue;
            if (p.hasPermission("spawnhelper.bypass")) continue;
            p.setGameMode(gm);
        }
    }

    private void applySpeedToSpawnPlayers(boolean enabled) {
        SpawnConfig cfg = plugin.getSpawnConfig();
        String world = cfg.getSpawnWorld();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.getWorld().getName().equals(world)) continue;
            if (p.hasPermission("spawnhelper.bypass")) continue;
            if (enabled) {
                p.setWalkSpeed(cfg.getWalkSpeed());
                p.setFlySpeed(cfg.getFlySpeed());
            } else {
                p.setWalkSpeed(0.2f);
                p.setFlySpeed(0.1f);
            }
        }
    }

    // ── Cycle helpers ─────────────────────────────────────────────────────────

    private static long nextTimePreset(long current) {
        for (int i = 0; i < TIME_PRESETS.length; i++) {
            if (current == TIME_PRESETS[i]) return TIME_PRESETS[(i + 1) % TIME_PRESETS.length];
        }
        return 6000L;
    }

    private static GameMode nextGamemode(GameMode current) {
        for (int i = 0; i < GAMEMODES.length; i++) {
            if (current == GAMEMODES[i]) return GAMEMODES[(i + 1) % GAMEMODES.length];
        }
        return GameMode.ADVENTURE;
    }
}
