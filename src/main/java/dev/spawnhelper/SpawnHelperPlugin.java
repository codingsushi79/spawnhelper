package dev.spawnhelper;

import dev.spawnhelper.command.SpawnCommand;
import dev.spawnhelper.command.SpawnHelperCommand;
import dev.spawnhelper.gui.GuiListener;
import dev.spawnhelper.listeners.BlockListener;
import dev.spawnhelper.listeners.CommandListener;
import dev.spawnhelper.listeners.DamageListener;
import dev.spawnhelper.listeners.PlayerListener;
import dev.spawnhelper.listeners.WorldListener;
import dev.spawnhelper.manager.SpawnCommandManager;
import dev.spawnhelper.manager.WorldStateManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class SpawnHelperPlugin extends JavaPlugin {

    private SpawnConfig spawnConfig;
    private CollisionManager collisionManager;
    private WorldStateManager worldStateManager;
    private SpawnCommandManager spawnCommandManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        spawnConfig          = new SpawnConfig(this);
        collisionManager     = new CollisionManager(this);
        worldStateManager    = new WorldStateManager(this);
        spawnCommandManager  = new SpawnCommandManager(this);

        // Listeners
        var pm = getServer().getPluginManager();
        pm.registerEvents(new DamageListener(this),  this);
        pm.registerEvents(new BlockListener(this),   this);
        pm.registerEvents(new PlayerListener(this),  this);
        pm.registerEvents(new WorldListener(this),   this);
        pm.registerEvents(new CommandListener(this), this);
        pm.registerEvents(new GuiListener(this),          this);
        pm.registerEvents(spawnCommandManager,             this);

        // Commands
        registerCommand("spawnhelper", new SpawnHelperCommand(this));
        registerCommand("spawn",       new SpawnCommand(this));

        worldStateManager.start();

        getLogger().info("Enabled — spawn world: \"" + spawnConfig.getSpawnWorld() + "\"");
    }

    @Override
    public void onDisable() {
        if (worldStateManager    != null) worldStateManager.stop();
        if (collisionManager     != null) collisionManager.cleanup();
        if (spawnCommandManager  != null) spawnCommandManager.cleanup();
        getLogger().info("SpawnHelper disabled.");
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    public SpawnConfig getSpawnConfig()               { return spawnConfig; }
    public CollisionManager getCollisionManager()      { return collisionManager; }
    public WorldStateManager getWorldStateManager()    { return worldStateManager; }
    public SpawnCommandManager getSpawnCommandManager(){ return spawnCommandManager; }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void registerCommand(String name, Object executor) {
        var cmd = getCommand(name);
        if (cmd == null) { getLogger().warning("Command \"" + name + "\" not in plugin.yml!"); return; }
        if (executor instanceof org.bukkit.command.CommandExecutor ce) cmd.setExecutor(ce);
        if (executor instanceof org.bukkit.command.TabCompleter tc)    cmd.setTabCompleter(tc);
    }
}
