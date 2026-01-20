package fun.ogtimes.skywars.spigot;

import fun.ogtimes.skywars.spigot.abilities.AbilityManager;
import fun.ogtimes.skywars.spigot.arena.Arena;
import fun.ogtimes.skywars.spigot.arena.ArenaManager;
import fun.ogtimes.skywars.spigot.arena.ArenaState;
import fun.ogtimes.skywars.spigot.arena.chest.ChestTypeManager;
import fun.ogtimes.skywars.spigot.box.BoxManager;
import fun.ogtimes.skywars.spigot.commands.CmdExecutor;
import fun.ogtimes.skywars.spigot.commands.user.CmdOthers;
import fun.ogtimes.skywars.spigot.config.ConfigManager;
import fun.ogtimes.skywars.spigot.database.DatabaseHandler;
import fun.ogtimes.skywars.spigot.events.EventsManager;
import fun.ogtimes.skywars.spigot.events.enums.ArenaLeaveCause;
import fun.ogtimes.skywars.spigot.kit.KitManager;
import fun.ogtimes.skywars.spigot.listener.*;
import fun.ogtimes.skywars.spigot.listener.skywars.ArenaListener;
import fun.ogtimes.skywars.spigot.listener.skywars.DeathListener;
import fun.ogtimes.skywars.spigot.listener.skywars.SpectateListener;
import fun.ogtimes.skywars.spigot.menus2.MenuListener;
import fun.ogtimes.skywars.spigot.player.SkyPlayer;
import fun.ogtimes.skywars.spigot.server.ServerManager;
import fun.ogtimes.skywars.spigot.server.SkyServer;
import fun.ogtimes.skywars.spigot.sign.SignManager;
import fun.ogtimes.skywars.spigot.utils.*;
import fun.ogtimes.skywars.spigot.utils.economy.SkyEconomyManager;
import fun.ogtimes.skywars.spigot.utils.sky.SkyHologram;
import fun.ogtimes.skywars.spigot.utils.sky.SkyScoreboard;

import java.io.*;
import java.lang.reflect.Field;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.*;

import fun.ogtimes.skywars.spigot.utils.variable.VariableManager;
import fun.ogtimes.skywars.spigot.utils.variable.VariablesDefault;
import fun.ogtimes.skywars.spigot.utils.variable.VariablesPlaceholder;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.server.*;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

@Getter @Setter
public class SkyWars extends JavaPlugin implements Listener {
    private static final ResourceBundle NULL_BUNDLE = null;

    @Getter
    private static SkyWars plugin;
    public static final HashMap<UUID, SkyPlayer> skyPlayersUUID;
    public static final HashMap<String, SkyPlayer> skyPlayers;
    @Getter
    public static Location spawn;
    public static final List<Location> hologram;

    public static final String arenas = "games";
    public static final String kits = "kits";
    public static final String chests = "chests";
    public static final String maps = "maps";
    public static String vupdate;
    public static String URL_KEY;

    public static FileConfiguration boxes;
    public static boolean holo;
    public static boolean update;
    public static boolean disabling;
    public static boolean login;
    public static boolean firstJoin;
    public static String prefix = "[SkyWars] ";
    public static long seconds;

    public static VariableManager variableManager;
    private static ResourceBundle messageBundle;
    private static ResourceBundle customBundle;
    private static DatabaseHandler databaseHandler;

    static {
        skyPlayersUUID = new HashMap<>();
        skyPlayers = new HashMap<>();
        hologram = new ArrayList<>();
        prefix = "[SkyWars] ";
        seconds = 0L;
    }

    private Metrics metrics;
    private BukkitAudiences adventure;

    public static void reloadMessages() {
        CustomConfig customConfig = new CustomConfig(SkyWars.getPlugin());
        CustomConfig messagesEn = new CustomConfig("messages_en");
        CustomConfig messagesEs = new CustomConfig("messages_es");
        CustomConfig messagesNl = new CustomConfig("messages_nl");

        customConfig.saveDefaultConfig(messagesEn);
        customConfig.saveDefaultConfig(messagesEs);
        customConfig.saveDefaultConfig(messagesNl);

        try {
            messageBundle = ResourceBundle.getBundle(
                    "messages",
                    new Locale(ConfigManager.main.getString("locale", "en")),
                    new UTF8Control()
            );
        } catch (MissingResourceException e) {
            messageBundle = NULL_BUNDLE;
        }

        try {
            customBundle = ResourceBundle.getBundle(
                    "messages",
                    new Locale(ConfigManager.main.getString("locale", "en")),
                    new FileResClassLoader(SkyWars.class.getClassLoader()),
                    new UTF8Control()
            );
        } catch (MissingResourceException e) {
            customBundle = NULL_BUNDLE;
        }
    }

    public static void reloadAbilities() {
        AbilityManager.initAbilities();
    }

    public static void reloadConfigMain() {
        ConfigManager.mainConfig();
        if (!ConfigManager.main.getString("spawn").isEmpty()) {
            String spawnStr = ConfigManager.main.getString("spawn");
            spawn = LocationUtil.getLocation(spawnStr);
        } else {
            spawn = Bukkit.getWorlds().getFirst().getSpawnLocation();
        }
    }

    public static void reloadConfigScoreboard() {
        ConfigManager.scoreboardConfig();
        SkyHologram.reloadHolograms();
    }

    public static void reloadConfigAbilities() {
        ConfigManager.abilitiesConfig();
    }

    public static void reloadConfigShop() {
        ConfigManager.shopConfig();
    }

    private static String translate(String key) {
        try {
            return customBundle.getString(key);
        } catch (MissingResourceException e) {
            try {
                return messageBundle.getString(key);
            } catch (MissingResourceException e2) {
                return customBundle.getString(key);
            }
        }
    }

    public static String getMessage(Messages messages) {
        if (messageBundle.containsKey(messages.toString())) {
            String message = translate(messages.toString());
            if (message.equalsIgnoreCase("null")) {
                return "";
            }
            return ChatColor.translateAlternateColorCodes('&', message);
        }
        return messages.toString();
    }

    public static String getMapSet() {
        return ConfigManager.main.getString("mode.bungeemapset");
    }

    @Nullable
    public static SkyPlayer getSkyPlayer(Player player) {
        if (player == null) {
            logError("Trying to get null player");
            return null;
        }
        return skyPlayersUUID.getOrDefault(
                player.getUniqueId(),
                skyPlayers.getOrDefault(player.getName(), null)
        );
    }

    public static List<Location> getHoloLocations() {
        return hologram;
    }

    public static void log(String message) {
        if (isDebug()) {
            System.out.println("[SkyWars] " + message);
        }
    }

    public static void logError(String message) {
        System.out.println("[SkyWars] ERROR: " + message);
    }

    public static boolean isLobbyMode() {
        String mode = ConfigManager.main.getString("mode.plugin");
        return mode.equalsIgnoreCase("Lobby") ||
                mode.equalsIgnoreCase("SkyWarsLobby") ||
                mode.startsWith("L");
    }

    public static boolean isProxyMode() {
        String mode = ConfigManager.main.getString("mode.plugin");
        return mode.equalsIgnoreCase("Bungee") ||
                mode.equalsIgnoreCase("BungeeMode") ||
                mode.startsWith("B");
    }

    public static boolean isMultiArenaMode() {
        String mode = ConfigManager.main.getString("mode.plugin");
        return mode.equalsIgnoreCase("Multi") ||
                mode.equalsIgnoreCase("MultiArena") ||
                mode.startsWith("M");
    }

    public static boolean isAutoStart() {
        return ConfigManager.main.getBoolean("mode.bungee-autostart");
    }

    public static boolean isRandomMap() {
        return ConfigManager.main.getBoolean("mode.bungeerandom");
    }

    public static boolean is18orHigher() {
        return true;
    }

    public static boolean isDebug() {
        return ConfigManager.main.getBoolean("debug");
    }

    public static void goToSpawn(SkyPlayer skyPlayer) {
        try {
            skyPlayer.teleport(spawn);
        } catch (Exception e) {
            logError("Lobby Spawn doesn't exist, please add a Lobby Spawn with: /sw lobbyspawn");
        }

        if (holo && !getHoloLocations().isEmpty()) {
            SkyHologram.createHologram(skyPlayer);
        }
    }

    public static void console(String message) {
        Bukkit.getServer().getConsoleSender().sendMessage(
                ChatColor.translateAlternateColorCodes('&', message)
        );
    }

    public static ChunkGenerator getVoidGenerator() {
        return new VoidUtil();
    }

    public static String getRandomLobby() {
        List<String> lobbies = ConfigManager.main.getStringList("lobbies_servers");
        SecureRandom random = new SecureRandom();
        int index = random.nextInt(lobbies.size());
        return lobbies.get(index);
    }

    public static boolean getMysql() {
        return ConfigManager.main.getString("data.type").equalsIgnoreCase("MySQL");
    }

    public static boolean isServerEnabled() {
        return isProxyMode();
    }

    public static boolean getUpdate() {
        return update;
    }

    public static String checkUpdate() {
        /*

        Must implement this in some way.

        if (ConfigManager.main.getBoolean("check_updates")) {
            try {
                HttpsURLConnection connection = (HttpsURLConnection) new URL(
                        "https://api.spigotmc.org/legacy/update.php?resource=" + resourceId
                ).openConnection();

                int timeout = 1250;
                connection.setConnectTimeout(timeout);
                connection.setReadTimeout(timeout);

                String currentVersion = getPlugin().getDescription().getVersion();
                String[] currentParts = currentVersion.split("\\.");

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream())
                );
                String latestVersion = reader.readLine();
                String[] latestParts = latestVersion.split("\\.");

                if (!latestVersion.equals(currentVersion)) {
                    if (!latestParts[0].equals(currentParts[0])) {
                        return "§8[§7SkyWars§8] §aThere is an important §4MAJOR UPDATE §a(§e" +
                                latestVersion + "§a) Download here: §ehttps://spigotmc.org/resources/6525/";
                    }

                    if (!latestParts[1].equals(currentParts[1])) {
                        return "§8[§7SkyWars§8] §aThere is an §cIMPORTANT UPDATE §a(§e" +
                                latestVersion + "§a) Download here: §ehttps://spigotmc.org/resources/6525/";
                    }

                    if (latestParts.length > 2 && !latestParts[2].equals(currentParts[2])) {
                        return "§8[§7SkyWars§8] §aThere is a §6MINOR UPDATE §a(§e" +
                                latestVersion + "§a) Download here: §ehttps://spigotmc.org/resources/6525/";
                    }
                    return null;
                }
                connection.disconnect();
            } catch (Exception e) {
                Bukkit.getConsoleSender().sendMessage(
                        ChatColor.RED + "Failed to check for an update on SpigotMC.org! - " +
                                e.getLocalizedMessage()
                );
                return null;
            }
            return null;
        }
        */
        return null;
    }

    @Override
    public void onEnable() {
        plugin = this;
        login = false;
        disabling = false;

        adventure = BukkitAudiences.create(this);
        console(prefix + "&aLoading all config files");

        ConfigManager.mainConfig();
        ConfigManager.scoreboardConfig();
        ConfigManager.abilitiesConfig();
        ConfigManager.shopConfig();
        ConfigManager.signsConfig();

        variableManager = new VariableManager();
        variableManager.registerVariableReplacer(new VariablesDefault());

        File boxesFile = new File(getDataFolder(), "boxes.yml");
        if (!boxesFile.exists()) {
            saveResource("boxes.yml", false);
        }
        boxes = YamlConfiguration.loadConfiguration(boxesFile);

        // Create directories
        File arenasDir = new File(getDataFolder(), arenas);
        File kitsDir = new File(getDataFolder(), kits);
        File chestsDir = new File(getDataFolder(), chests);
        File mapsDir = new File(maps);

        if (!arenasDir.exists()) arenasDir.mkdirs();
        if (!mapsDir.exists()) mapsDir.mkdirs();

        if (!kitsDir.exists()) {
            kitsDir.mkdirs();
            // Save default kits
            saveResource("kits/Archer.yml", false);
            saveResource("kits/Blacksmith.yml", false);
            saveResource("kits/Bomber.yml", false);
            saveResource("kits/Builder.yml", false);
            saveResource("kits/Chicken.yml", false);
            saveResource("kits/Digger.yml", false);
            saveResource("kits/Enchanter.yml", false);
            saveResource("kits/Enderman.yml", false);
            saveResource("kits/Farmer.yml", false);
            saveResource("kits/Fisherman.yml", false);
            saveResource("kits/Healer.yml", false);
            saveResource("kits/Iron_golem.yml", false);
            saveResource("kits/Joker.yml", false);
            saveResource("kits/Lumberjack.yml", false);
            saveResource("kits/Noobly.yml", false);
            saveResource("kits/Pyromaniac.yml", false);
            saveResource("kits/Redstone_master.yml", false);
            saveResource("kits/Scout.yml", false);
            saveResource("kits/Spiderman.yml", false);
            saveResource("kits/Swordsman.yml", false);
        }

        if (!chestsDir.exists()) {
            chestsDir.mkdir();
            saveResource("chests/Basic.yml", false);
            saveResource("chests/Normal.yml", false);
            saveResource("chests/Overpowered.yml", false);
        }

        try {
            if (loadUpdate()) {
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        console(prefix + "&aEconomy: &e" + ConfigManager.main.getString("economy.mode"));

        // Register events
        Bukkit.getServer().getPluginManager().registerEvents(new LoginListener(), this);

        if (!isLobbyMode()) {
            if (!ConfigManager.main.getBoolean("options.disablePerWorldTab")) {
                Bukkit.getServer().getPluginManager().registerEvents(new WorldTabListener(), this);
            }
            Bukkit.getServer().getPluginManager().registerEvents(new TrailListener(), this);
        }

        Bukkit.getServer().getPluginManager().registerEvents(new SignManager(), this);
        Bukkit.getServer().getPluginManager().registerEvents(this, this);
        Bukkit.getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        Bukkit.getServer().getPluginManager().registerEvents(new InteractListener(), this);
        Bukkit.getServer().getPluginManager().registerEvents(new DamageListener(), this);
        Bukkit.getServer().getPluginManager().registerEvents(new WorldListener(), this);
        Bukkit.getServer().getPluginManager().registerEvents(new StatsListener(), this);
        Bukkit.getServer().getPluginManager().registerEvents(new AbilitiesListener(), this);
        Bukkit.getServer().getPluginManager().registerEvents(new EventsManager(), this);
        Bukkit.getServer().getPluginManager().registerEvents(new DeathListener(), this);
        Bukkit.getServer().getPluginManager().registerEvents(new SpectateListener(), this);
        Bukkit.getServer().getPluginManager().registerEvents(new ArenaListener(), this);
        Bukkit.getServer().getPluginManager().registerEvents(new MenuListener(), this);

        Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        reloadMessages();

        console(prefix + "&aLoading lang files");

        SkyEconomyManager.load();

        try {
            databaseHandler = new DatabaseHandler();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            log(e.getMessage());
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        if (isLobbyMode()) {
            Bukkit.getServer().getMessenger().registerIncomingPluginChannel(
                    this, "skywars:sign-update", new BungeeRecieveListener()
            );
            ServerManager.initServers();
        }

        if (isProxyMode()) {
            Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(this, "skywars:sign-send");
        }

        if (isServerEnabled()) {
            SkyServer.load();
        }

        KitManager.initKits();
        AbilityManager.initAbilities();

        if (!isLobbyMode()) {
            BoxManager.initBoxes();
            ChestTypeManager.loadChests();

            console(prefix + "&aLoading arenas (Games)");
            ArenaManager.initGames();
            console(prefix + "&e" + ArenaManager.getGames().size() + " arenas &ahave been enabled");

            RandomFirework.loadFireworks();

            new CmdOthers(this);
        }

        // Set spawn location
        if (!ConfigManager.main.getString("spawn").isEmpty()) {
            String spawnStr = ConfigManager.main.getString("spawn");
            spawn = LocationUtil.getLocation(spawnStr);
        } else {
            spawn = Bukkit.getWorlds().getFirst().getSpawnLocation();
        }

        // Check for HolographicDisplays
        holo = Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays");
        if (holo) {
            for (String loc : ConfigManager.score.getStringList("hologram.locations")) {
                hologram.add(LocationUtil.getLocation(loc));
            }
            console(prefix + "&aHolographicDisplays hook enabled (&e" +
                    ConfigManager.score.getStringList("hologram.locations").size() +
                    " &aHologram(s))");
        }

        getCommand("sw").setExecutor(new CmdExecutor());
        getCommand("sw").setTabCompleter(new CmdExecutor());

        Bukkit.setSpawnRadius(0);

        metrics = new Metrics(this, 1089);

        console(prefix + "&aMetrics (bStats) enabled");

        String server_version = Bukkit.getServer().getClass().getPackage().getName()
                .replace(".", ",").split(",")[3];

        log("Server Version: " + server_version);

        update = checkUpdate() != null;

        login = true;
        seconds = new Date().getTime();
        firstJoin = false;

        // Start scoreboard updater
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    SkyPlayer skyPlayer = getSkyPlayer(player);
                    if (skyPlayer == null) continue;
                    SkyScoreboard.contentBoard(skyPlayer);
                }
            }
        }.runTaskTimerAsynchronously(this, 0L, 15L);

        // Check for PlaceholderAPI
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            console(prefix + "&aEnabling PlaceholderAPI...");
            new VariablesPlaceholder(this).register();
            console(prefix + "&aPlaceholderAPI enabled, registered successfully");
        }

        registerGlow();

        if (isMultiArenaMode() || isLobbyMode()) {
            SignManager.loadSigns();
        }
    }

    @Override
    public void onDisable() {
        disabling = true;

        metrics.shutdown();

        if (adventure != null) {
            adventure.close();
        }

        for (SkyPlayer skyPlayer : skyPlayersUUID.values()) {
            if (skyPlayer.isInArena()) {
                Arena arena = skyPlayer.getArena();
                arena.removePlayer(skyPlayer, ArenaLeaveCause.RESTART);
            }
            skyPlayer.upload(true);
        }

        if (databaseHandler != null) {
            console(prefix + "&cDisabling all data");
            DatabaseHandler.getDS().close();
        }
    }

    public void reloadSigns() {
        SignManager.loadSigns();
    }

    public void reloadKits() {
        KitManager.initKits();
    }

    public void reloadBoxes() {
        BoxManager.initBoxes();
    }

    public void reloadChests() {
        ChestTypeManager.loadChests();
    }

    public void reloadArenas() {
        for (Arena arena : ArenaManager.getGames()) {
            arena.restart();
        }
        ArenaManager.initGames();
    }

    @EventHandler
    public void ping(ServerListPingEvent event) {
        if (isProxyMode()) {
            for (Arena arena : ArenaManager.getGames()) {
                event.setMaxPlayers(arena.getMaxPlayers());

                if (arena.isLoading()) {
                    event.setMotd(getMessage(Messages.MOTD_LOADING).replace("%map%", arena.getName()));
                    return;
                }

                if (arena.getState() == ArenaState.WAITING) {
                    event.setMotd(getMessage(Messages.MOTD_WAITING).replace("%map%", arena.getName()));
                }

                if (arena.getState() == ArenaState.STARTING) {
                    event.setMotd(getMessage(Messages.MOTD_STARTING).replace("%map%", arena.getName()));
                }

                if (arena.getState() == ArenaState.INGAME) {
                    event.setMotd(getMessage(Messages.MOTD_INGAME).replace("%map%", arena.getName()));
                }

                if (arena.getState() == ArenaState.ENDING) {
                    event.setMotd(getMessage(Messages.MOTD_ENDING).replace("%map%", arena.getName()));
                }
            }
        }
    }

    private boolean loadUpdate() {
        return false;
    }

    private void registerGlow() {
        try {
            Field field = Enchantment.class.getDeclaredField("acceptingNew");
            field.setAccessible(true);
            field.set(null, true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Glow glow = new Glow(120);
            Enchantment.registerEnchantment(glow);
        } catch (IllegalArgumentException ignored) {
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}