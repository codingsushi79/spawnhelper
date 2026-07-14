package dev.spawnhelper;

import org.bukkit.GameMode;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

/**
 * Typed accessor layer over config.yml.
 *
 * <p>All setters go through {@link #set(String, Object)}, which writes the value
 * into the in-memory config. The caller (usually the GUI listener) is responsible
 * for calling {@code plugin.saveConfig()} once per interaction.</p>
 */
public class SpawnConfig {

    private final SpawnHelperPlugin plugin;

    public SpawnConfig(SpawnHelperPlugin plugin) {
        this.plugin = plugin;
    }

    private FileConfiguration cfg() {
        return plugin.getConfig();
    }

    // ── World ─────────────────────────────────────────────────────────────────

    public String getSpawnWorld() {
        return cfg().getString("spawn-world", "world");
    }

    // ── Basic protection ──────────────────────────────────────────────────────

    public boolean isNoDamage()              { return cfg().getBoolean("settings.no-damage",           true); }
    public boolean isNoPvp()                 { return cfg().getBoolean("settings.no-pvp",              true); }
    public boolean isNoBlockBreaking()       { return cfg().getBoolean("settings.no-block-breaking",   true); }
    public boolean isNoBlockPlacing()        { return cfg().getBoolean("settings.no-block-placing",    true); }
    public boolean isAllowBlockInteraction() { return cfg().getBoolean("settings.allow-block-interaction", true); }
    public boolean isNoCollision()           { return cfg().getBoolean("settings.no-collision",        true); }
    public boolean isNoHunger()              { return cfg().getBoolean("settings.no-hunger",           true); }
    public boolean isNoItemDrop()            { return cfg().getBoolean("settings.no-item-drop",        false); }
    public boolean isNoItemPickup()          { return cfg().getBoolean("settings.no-item-pickup",      false); }
    public boolean isAllowFlight()           { return cfg().getBoolean("settings.allow-flight",        false); }
    public boolean isNoMobSpawning()         { return cfg().getBoolean("settings.no-mob-spawning",     false); }
    public boolean isNoFireSpread()          { return cfg().getBoolean("settings.no-fire-spread",      true); }
    public boolean isNoExplosionDamage()     { return cfg().getBoolean("settings.no-explosion-damage", true); }

    // ── Time lock ─────────────────────────────────────────────────────────────

    public boolean isTimeLockEnabled() { return cfg().getBoolean("settings.time-lock.enabled", false); }
    public long    getLockedTime()     { return cfg().getLong("settings.time-lock.time", 6000L); }

    // ── Weather lock ──────────────────────────────────────────────────────────

    public boolean isWeatherLock() { return cfg().getBoolean("settings.weather-lock", false); }

    // ── Gamemode enforcement ──────────────────────────────────────────────────

    public boolean isGamemodeEnforced() { return cfg().getBoolean("settings.gamemode-enforcement.enabled", false); }
    public boolean isGamemodeRestoreOnLeave() { return cfg().getBoolean("settings.gamemode-enforcement.restore-on-leave", true); }

    public GameMode getEnforcedGamemode() {
        String raw = cfg().getString("settings.gamemode-enforcement.gamemode", "ADVENTURE");
        try {
            return GameMode.valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException e) {
            return GameMode.ADVENTURE;
        }
    }

    // ── Speed modifier ────────────────────────────────────────────────────────

    public boolean isSpeedEnabled() { return cfg().getBoolean("settings.speed.enabled", false); }
    public float   getWalkSpeed()   { return (float) cfg().getDouble("settings.speed.walk", 0.2); }
    public float   getFlySpeed()    { return (float) cfg().getDouble("settings.speed.fly",  0.1); }

    // ── Greeting ──────────────────────────────────────────────────────────────

    public boolean isGreetingEnabled()    { return cfg().getBoolean("greeting.enabled", true); }
    public String  getGreetingTitle()     { return cfg().getString("greeting.title",    "&6Welcome to Spawn!"); }
    public String  getGreetingSubtitle()  { return cfg().getString("greeting.subtitle", "&7Enjoy your stay"); }
    public String  getGreetingActionBar() { return cfg().getString("greeting.action-bar", ""); }
    public String  getGreetingChat()      { return cfg().getString("greeting.chat-message", ""); }
    public int     getGreetingFadeIn()    { return cfg().getInt("greeting.fade-in", 10); }
    public int     getGreetingStay()      { return cfg().getInt("greeting.stay",    60); }
    public int     getGreetingFadeOut()   { return cfg().getInt("greeting.fade-out", 10); }

    // ── Command blacklist ─────────────────────────────────────────────────────

    public boolean isCommandBlacklistEnabled() {
        return cfg().getBoolean("settings.command-blacklist.enabled", false);
    }

    public List<String> getBlacklistedCommands() {
        return cfg().getStringList("settings.command-blacklist.commands");
    }

    // ── Mutation (used by GUI) ────────────────────────────────────────────────

    /** Set a value in the in-memory config. Caller must call {@code plugin.saveConfig()} afterwards. */
    public void set(String path, Object value) {
        plugin.getConfig().set(path, value);
    }

    // ── Spawn command ──────────────────────────────────────────────────────────

    public boolean isSpawnCommandEnabled()  { return cfg().getBoolean("spawn-command.enabled",          true); }
    public boolean isSpawnUseWorldSpawn()   { return cfg().getBoolean("spawn-command.use-world-spawn",  true); }
    public double  getSpawnDestX()          { return cfg().getDouble("spawn-command.destination.x",     0.5); }
    public double  getSpawnDestY()          { return cfg().getDouble("spawn-command.destination.y",     64.0); }
    public double  getSpawnDestZ()          { return cfg().getDouble("spawn-command.destination.z",     0.5); }
    public float   getSpawnDestYaw()        { return (float) cfg().getDouble("spawn-command.destination.yaw",   0.0); }
    public float   getSpawnDestPitch()      { return (float) cfg().getDouble("spawn-command.destination.pitch", 0.0); }
    public int     getSpawnDelay()          { return cfg().getInt("spawn-command.delay",    3); }
    public boolean isSpawnCancelOnMove()    { return cfg().getBoolean("spawn-command.cancel-on-move", true); }
    public int     getSpawnCooldown()       { return cfg().getInt("spawn-command.cooldown", 30); }

    public String getSpawnTeleportingMessage() {
        return cfg().getString("spawn-command.messages.teleporting",
                "&eTeleporting to spawn in &6{time}s&e...");
    }
    public String getSpawnCancelledMessage() {
        return cfg().getString("spawn-command.messages.cancelled",
                "&cTeleport cancelled! You moved.");
    }
    public String getSpawnCooldownMessage() {
        return cfg().getString("spawn-command.messages.on-cooldown",
                "&cYou must wait &e{time} &cbefore using /spawn again.");
    }
    public String getSpawnTeleportedMessage() {
        return cfg().getString("spawn-command.messages.teleported",
                "&aYou have been teleported to spawn.");
    }

    // ── Reload ────────────────────────────────────────────────────────────────

    public void reload() {
        plugin.reloadConfig();
    }
}
