package dev.spawnhelper.command;

import dev.spawnhelper.SpawnHelperPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpawnCommand implements CommandExecutor {

    private final SpawnHelperPlugin plugin;

    public SpawnCommand(SpawnHelperPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use /spawn.");
            return true;
        }
        plugin.getSpawnCommandManager().startTeleport(player);
        return true;
    }
}
