package dev.spawnhelper;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

/**
 * Manages the no-collision scoreboard team.
 *
 * <p>Players are added when they enter the spawn world and removed when they
 * leave. The team rule {@code COLLISION_RULE = NEVER} suppresses collisions
 * client-side without any NMS or packet code.</p>
 */
public class CollisionManager {

    private static final String TEAM_NAME = "sh_nocollide";

    private final SpawnHelperPlugin plugin;
    private final Team team;

    public CollisionManager(SpawnHelperPlugin plugin) {
        this.plugin = plugin;

        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
        Team existing = board.getTeam(TEAM_NAME);
        team = (existing != null) ? existing : board.registerNewTeam(TEAM_NAME);

        team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        team.setCanSeeFriendlyInvisibles(false);
    }

    public void addPlayer(Player player) {
        if (plugin.getSpawnConfig().isNoCollision()) {
            team.addPlayer(player);
        }
    }

    public void removePlayer(Player player) {
        team.removePlayer(player);
    }

    /**
     * Re-evaluate all online players after the setting is toggled via the GUI.
     * Adds spawn-world players if enabled, removes them if disabled.
     */
    public void reapply() {
        boolean enabled = plugin.getSpawnConfig().isNoCollision();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (plugin.getSpawnConfig().isInSpawnArea(player.getLocation())) {
                if (enabled) {
                    team.addPlayer(player);
                } else {
                    team.removePlayer(player);
                }
            }
        }
    }

    /** Remove all online players from the team on plugin disable. */
    public void cleanup() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            team.removePlayer(player);
        }
    }
}
