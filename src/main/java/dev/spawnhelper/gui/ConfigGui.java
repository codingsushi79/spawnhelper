package dev.spawnhelper.gui;

import dev.spawnhelper.SpawnConfig;
import dev.spawnhelper.SpawnHelperPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds and owns the SpawnHelper configuration GUI.
 *
 * <p>Use {@link ConfigGui} as the {@link InventoryHolder} so the {@link GuiListener}
 * can identify the inventory reliably without string-matching the title.</p>
 *
 * <p>Layout (5 rows = 45 slots):
 * <pre>
 * [=][=][=][=][★][=][=][=][=]
 * [=][D][P][B][L][I][C][H][=]   combat & blocks
 * [=][R][K][F][M][S][E][T][=]   player & world
 * [=][W][G][V][Gr][X][·][·][=]  extras
 * [=][=][=][=][✗][=][=][=][=]
 * </pre>
 * </p>
 */
public class ConfigGui implements InventoryHolder {

    // ── Slot constants ────────────────────────────────────────────────────────
    public static final int SLOT_NO_DAMAGE        = 10;
    public static final int SLOT_NO_PVP           = 11;
    public static final int SLOT_NO_BREAK         = 12;
    public static final int SLOT_NO_PLACE         = 13;
    public static final int SLOT_ALLOW_INTERACT   = 14;
    public static final int SLOT_NO_COLLISION     = 15;
    public static final int SLOT_NO_HUNGER        = 16;

    public static final int SLOT_NO_ITEM_DROP     = 19;
    public static final int SLOT_NO_ITEM_PICKUP   = 20;
    public static final int SLOT_ALLOW_FLIGHT     = 21;
    public static final int SLOT_NO_MOB_SPAWN     = 22;
    public static final int SLOT_NO_FIRE_SPREAD   = 23;
    public static final int SLOT_NO_EXPLOSION     = 24;
    public static final int SLOT_TIME_LOCK        = 25;

    public static final int SLOT_WEATHER_LOCK     = 28;
    public static final int SLOT_GAMEMODE         = 29;
    public static final int SLOT_SPEED            = 30;
    public static final int SLOT_GREETING         = 31;
    public static final int SLOT_CMD_BLACKLIST    = 32;
    public static final int SLOT_SPAWN_CMD        = 33;

    public static final int SLOT_CLOSE            = 40;

    // ── State ─────────────────────────────────────────────────────────────────
    private final SpawnHelperPlugin plugin;
    private Inventory inventory;

    public ConfigGui(SpawnHelperPlugin plugin) {
        this.plugin = plugin;
    }

    // ── Public API ────────────────────────────────────────────────────────────

    public void open(org.bukkit.entity.Player player) {
        inventory = buildInventory();
        player.openInventory(inventory);
    }

    /** Re-render all setting items in place (called after every toggle). */
    public void refresh() {
        if (inventory != null) populate(inventory);
    }

    // ── Build ─────────────────────────────────────────────────────────────────

    private Inventory buildInventory() {
        Component title = Component.text()
                .append(Component.text("SpawnHelper ", NamedTextColor.GOLD, TextDecoration.BOLD))
                .append(Component.text("» ", NamedTextColor.DARK_GRAY))
                .append(Component.text("Config", NamedTextColor.GRAY))
                .build();

        Inventory inv = Bukkit.createInventory(this, 45, title);

        // Border
        ItemStack pane = border();
        int[] borderSlots = {0,1,2,3,4,5,6,7,8, 9,17, 18,26, 27,35, 36,37,38,39,40,41,42,43,44};
        for (int s : borderSlots) inv.setItem(s, pane);

        // Header label
        inv.setItem(4, label(Material.NETHER_STAR,
                comp("SpawnHelper", NamedTextColor.AQUA, TextDecoration.BOLD),
                List.of(
                        gray("Spawn world: ").append(white(plugin.getSpawnConfig().getSpawnWorld())),
                        dim("Left-click items to toggle"),
                        dim("Right-click some items to cycle values")
                )));

        // Close button
        inv.setItem(SLOT_CLOSE, closeBtn());

        populate(inv);
        return inv;
    }

    private void populate(Inventory inv) {
        SpawnConfig c = plugin.getSpawnConfig();

        // ── Row 1: Combat & Blocks ────────────────────────────────────────
        inv.setItem(SLOT_NO_DAMAGE,      toggle(Material.SHIELD,          "No Damage",         c.isNoDamage(),
                "Prevent all player damage",
                "(fall, fire, suffocation, mobs)"));
        inv.setItem(SLOT_NO_PVP,         toggle(Material.IRON_SWORD,      "No PvP",            c.isNoPvp(),
                "Prevent player-vs-player hits"));
        inv.setItem(SLOT_NO_BREAK,       toggle(Material.DIAMOND_PICKAXE, "No Block Breaking", c.isNoBlockBreaking(),
                "Players cannot break blocks"));
        inv.setItem(SLOT_NO_PLACE,       toggle(Material.GRASS_BLOCK,     "No Block Placing",  c.isNoBlockPlacing(),
                "Players cannot place blocks"));
        inv.setItem(SLOT_ALLOW_INTERACT, toggle(Material.OAK_DOOR,        "Allow Interaction", c.isAllowBlockInteraction(),
                "Allow right-clicking blocks",
                "(doors, chests, buttons…)"));
        inv.setItem(SLOT_NO_COLLISION,   toggle(Material.FEATHER,         "No Collision",      c.isNoCollision(),
                "Players walk through each other",
                "(scoreboard team rule)"));
        inv.setItem(SLOT_NO_HUNGER,      toggle(Material.COOKED_BEEF,     "No Hunger",         c.isNoHunger(),
                "Freeze the hunger bar"));

        // ── Row 2: Player & World ─────────────────────────────────────────
        inv.setItem(SLOT_NO_ITEM_DROP,   toggle(Material.HOPPER,   "No Item Drop",        c.isNoItemDrop(),
                "Prevent players from dropping items"));
        inv.setItem(SLOT_NO_ITEM_PICKUP, toggle(Material.CHEST,    "No Item Pickup",      c.isNoItemPickup(),
                "Prevent players from picking up items"));
        inv.setItem(SLOT_ALLOW_FLIGHT,   toggle(Material.ELYTRA,   "Allow Flight",        c.isAllowFlight(),
                "Grant flight on entry",
                "Revoked when leaving spawn"));
        inv.setItem(SLOT_NO_MOB_SPAWN,   toggle(Material.SPAWNER,  "No Mob Spawning",     c.isNoMobSpawning(),
                "Block natural mob spawning"));
        inv.setItem(SLOT_NO_FIRE_SPREAD, toggle(Material.FIRE_CHARGE, "No Fire Spread",   c.isNoFireSpread(),
                "Fire cannot spread or burn blocks"));
        inv.setItem(SLOT_NO_EXPLOSION,   toggle(Material.TNT,      "No Explosion Damage", c.isNoExplosionDamage(),
                "Explosions don't damage blocks"));
        inv.setItem(SLOT_TIME_LOCK,      timeLockItem(c));

        // ── Row 3: Extra ──────────────────────────────────────────────────
        inv.setItem(SLOT_WEATHER_LOCK,   toggle(Material.BUCKET,  "Weather Lock",      c.isWeatherLock(),
                "Keep weather always clear"));
        inv.setItem(SLOT_GAMEMODE,       gamemodeItem(c));
        inv.setItem(SLOT_SPEED,          speedItem(c));
        inv.setItem(SLOT_GREETING,       toggle(Material.PAPER,   "Greeting",          c.isGreetingEnabled(),
                "Show title/message on world entry",
                "Edit text in config.yml"));
        inv.setItem(SLOT_CMD_BLACKLIST,  toggle(Material.BARRIER, "Command Blacklist", c.isCommandBlacklistEnabled(),
                "Block commands listed in config.yml"));
        inv.setItem(SLOT_SPAWN_CMD,      spawnCmdItem(c));
    }

    // ── Specialised item builders ─────────────────────────────────────────────

    private ItemStack spawnCmdItem(SpawnConfig c) {
        boolean on = c.isSpawnCommandEnabled();
        String dest = c.isSpawnUseWorldSpawn()
                ? "World spawn"
                : String.format("%.1f, %.1f, %.1f", c.getSpawnDestX(), c.getSpawnDestY(), c.getSpawnDestZ());
        ItemStack item = new ItemStack(Material.ENDER_PEARL);
        ItemMeta m = item.getItemMeta();
        m.displayName(statusName("/spawn Command", on));
        m.lore(List.of(
                statusLine(on),
                gray("Destination: ").append(white(dest)),
                gray("Delay: ").append(white(c.getSpawnDelay() + "s")),
                gray("Cooldown: ").append(white(c.getSpawnCooldown() == 0 ? "none" : c.getSpawnCooldown() + "s")),
                gray("Cancel on move: ").append(c.isSpawnCancelOnMove()
                        ? Component.text("yes", NamedTextColor.GREEN)
                        : Component.text("no",  NamedTextColor.RED)),
                Component.empty(),
                hint("Left-click \u2014 toggle on/off"),
                hint("Edit full settings in config.yml")
        ));
        applyGlow(m, on);
        item.setItemMeta(m);
        return item;
    }

    private static ItemStack timeLockItem(SpawnConfig c) {
        boolean on = c.isTimeLockEnabled();
        long t = c.getLockedTime();
        ItemStack item = new ItemStack(Material.CLOCK);
        ItemMeta m = item.getItemMeta();
        m.displayName(statusName("Time Lock", on));
        m.lore(List.of(
                statusLine(on),
                gray("Time: ").append(white(timeLabel(t) + " (" + t + ")")),
                Component.empty(),
                hint("Left-click — toggle on/off"),
                hint("Right-click — cycle time preset")
        ));
        applyGlow(m, on);
        item.setItemMeta(m);
        return item;
    }

    private static ItemStack gamemodeItem(SpawnConfig c) {
        boolean on = c.isGamemodeEnforced();
        GameMode gm = c.getEnforcedGamemode();
        Material mat = switch (gm) {
            case CREATIVE  -> Material.GOLDEN_SWORD;
            case ADVENTURE -> Material.COMPASS;
            case SPECTATOR -> Material.ENDER_EYE;
            default        -> Material.IRON_SWORD;
        };
        ItemStack item = new ItemStack(mat);
        ItemMeta m = item.getItemMeta();
        m.displayName(statusName("Gamemode Enforce", on));
        m.lore(List.of(
                statusLine(on),
                gray("Gamemode: ").append(white(gm.name())),
                Component.empty(),
                hint("Left-click — toggle on/off"),
                hint("Right-click — cycle gamemode")
        ));
        applyGlow(m, on);
        m.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(m);
        return item;
    }

    private static ItemStack speedItem(SpawnConfig c) {
        boolean on = c.isSpeedEnabled();
        ItemStack item = new ItemStack(Material.SUGAR);
        ItemMeta m = item.getItemMeta();
        m.displayName(statusName("Speed Modifier", on));
        m.lore(List.of(
                statusLine(on),
                gray("Walk: ").append(white(String.valueOf(c.getWalkSpeed()))),
                gray("Fly:  ").append(white(String.valueOf(c.getFlySpeed()))),
                Component.empty(),
                hint("Left-click — toggle on/off"),
                hint("Edit values in config.yml")
        ));
        applyGlow(m, on);
        item.setItemMeta(m);
        return item;
    }

    // ── Generic toggle builder ────────────────────────────────────────────────

    private static ItemStack toggle(Material mat, String name, boolean on, String... desc) {
        ItemStack item = new ItemStack(mat);
        ItemMeta m = item.getItemMeta();
        m.displayName(statusName(name, on));
        List<Component> lore = new ArrayList<>();
        lore.add(statusLine(on));
        lore.add(hint("Left-click — toggle"));
        lore.add(Component.empty());
        for (String line : desc) lore.add(gray(line));
        m.lore(lore);
        applyGlow(m, on);
        m.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(m);
        return item;
    }

    // ── Decorative items ──────────────────────────────────────────────────────

    private static ItemStack border() {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta m = item.getItemMeta();
        m.displayName(Component.empty());
        item.setItemMeta(m);
        return item;
    }

    private static ItemStack label(Material mat, Component name, List<Component> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta m = item.getItemMeta();
        m.displayName(name);
        m.lore(lore);
        m.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(m);
        return item;
    }

    private static ItemStack closeBtn() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta m = item.getItemMeta();
        m.displayName(comp("Close", NamedTextColor.RED, TextDecoration.BOLD));
        item.setItemMeta(m);
        return item;
    }

    // ── Component helpers ─────────────────────────────────────────────────────

    private static Component statusName(String name, boolean on) {
        return comp(name, on ? NamedTextColor.GREEN : NamedTextColor.RED, TextDecoration.BOLD);
    }

    private static Component statusLine(boolean on) {
        return gray("Status: ").append(on
                ? Component.text("Enabled",  NamedTextColor.GREEN)
                : Component.text("Disabled", NamedTextColor.RED));
    }

    private static Component comp(String text, NamedTextColor color, TextDecoration deco) {
        return Component.text(text, color).decoration(TextDecoration.ITALIC, false).decorate(deco);
    }

    private static Component gray(String text) {
        return Component.text(text, NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false);
    }

    private static Component white(String text) {
        return Component.text(text, NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false);
    }

    private static Component dim(String text) {
        return Component.text(text, NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false);
    }

    private static Component hint(String text) {
        return Component.text(text, NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false);
    }

    private static void applyGlow(ItemMeta m, boolean glow) {
        if (glow) {
            m.addEnchant(Enchantment.UNBREAKING, 1, true);
            m.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        } else {
            m.removeEnchant(Enchantment.UNBREAKING);
        }
    }

    static String timeLabel(long t) {
        if (t < 1000)  return "Sunrise";
        if (t < 6000)  return "Morning";
        if (t < 7000)  return "Noon";
        if (t < 12000) return "Afternoon";
        if (t < 13800) return "Sunset";
        if (t < 18000) return "Night";
        return "Midnight";
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
