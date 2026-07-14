package dev.spawnhelper.command;

import dev.spawnhelper.SpawnConfig;
import dev.spawnhelper.SpawnHelperPlugin;
import dev.spawnhelper.gui.ConfigGui;
import dev.spawnhelper.manager.SpawnCommandManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SpawnHelperCommand implements CommandExecutor, TabCompleter {

    private static final String PREFIX = "§6[SpawnHelper]§r ";
    private final SpawnHelperPlugin plugin;

    public SpawnHelperCommand(SpawnHelperPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("spawnhelper.admin")) {
            sender.sendMessage(PREFIX + "§cYou don't have permission.");
            return true;
        }

        if (args.length == 0) { sendHelp(sender, label); return true; }

        switch (args[0].toLowerCase()) {
            case "clearcooldown" -> {
                if (args.length < 2) {
                    sender.sendMessage(PREFIX + "§cUsage: /spawnhelper clearcooldown <player>");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(PREFIX + "§cPlayer §f" + args[1] + "§c not found.");
                    return true;
                }
                plugin.getSpawnCommandManager().clearCooldown(target.getUniqueId());
                sender.sendMessage(PREFIX + "§aCleared spawn cooldown for §f" + target.getName() + "§a.");
                target.sendMessage(PREFIX + "§aYour /spawn cooldown has been cleared by an admin.");
            }
            case "gui" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(PREFIX + "§cOnly players can open the GUI.");
                    return true;
                }
                new ConfigGui(plugin).open(player);
            }
            case "reload" -> {
                plugin.getSpawnConfig().reload();
                plugin.getCollisionManager().cleanup();
                plugin.getWorldStateManager().restart();
                sender.sendMessage(PREFIX + "§aConfiguration reloaded.");
            }
            case "info" -> sendInfo(sender);
            default -> sendHelp(sender, label);
        }
        return true;
    }

    private void sendHelp(CommandSender sender, String label) {
        sender.sendMessage("§6§l=== SpawnHelper Help ===");
        sender.sendMessage("§7/" + label + " gui                    §f— Open the configuration GUI");
        sender.sendMessage("§7/" + label + " reload                 §f— Reload config.yml");
        sender.sendMessage("§7/" + label + " info                   §f— Show active settings");
        sender.sendMessage("§7/" + label + " clearcooldown <player> §f— Clear a player's /spawn cooldown");
    }

    private void sendInfo(CommandSender sender) {
        SpawnConfig c = plugin.getSpawnConfig();
        sender.sendMessage("§6§l=== SpawnHelper Settings ===");
        sender.sendMessage(row("Spawn world",         c.getSpawnWorld()));
        sender.sendMessage(row("No damage",           c.isNoDamage()));
        sender.sendMessage(row("No PvP",              c.isNoPvp()));
        sender.sendMessage(row("No block breaking",   c.isNoBlockBreaking()));
        sender.sendMessage(row("No block placing",    c.isNoBlockPlacing()));
        sender.sendMessage(row("Allow interaction",   c.isAllowBlockInteraction()));
        sender.sendMessage(row("No collision",        c.isNoCollision()));
        sender.sendMessage(row("No hunger",           c.isNoHunger()));
        sender.sendMessage(row("No item drop",        c.isNoItemDrop()));
        sender.sendMessage(row("No item pickup",      c.isNoItemPickup()));
        sender.sendMessage(row("Allow flight",        c.isAllowFlight()));
        sender.sendMessage(row("No mob spawning",     c.isNoMobSpawning()));
        sender.sendMessage(row("No fire spread",      c.isNoFireSpread()));
        sender.sendMessage(row("No explosion damage", c.isNoExplosionDamage()));
        sender.sendMessage(row("Time lock",           c.isTimeLockEnabled() ? "enabled (" + c.getLockedTime() + ")" : "disabled"));
        sender.sendMessage(row("Weather lock",        c.isWeatherLock()));
        sender.sendMessage(row("Gamemode enforce",    c.isGamemodeEnforced() ? "enabled (" + c.getEnforcedGamemode() + ")" : "disabled"));
        sender.sendMessage(row("Speed modifier",      c.isSpeedEnabled() ? "enabled (walk=" + c.getWalkSpeed() + " fly=" + c.getFlySpeed() + ")" : "disabled"));
        sender.sendMessage(row("Greeting",            c.isGreetingEnabled()));
        sender.sendMessage(row("Command blacklist",   c.isCommandBlacklistEnabled()));
    }

    private static String row(String key, boolean value) {
        return "§7" + key + ": " + (value ? "§ayes" : "§cno");
    }

    private static String row(String key, String value) {
        return "§7" + key + ": §f" + value;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("spawnhelper.admin")) return Collections.emptyList();
        if (args.length == 1) return Arrays.asList("gui", "reload", "info", "clearcooldown");
        if (args.length == 2 && args[0].equalsIgnoreCase("clearcooldown")) {
            String prefix = args[1].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(prefix))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
