package fun.ogtimes.skywars.spigot.arena;

import fun.ogtimes.skywars.spigot.SkyWars;
import fun.ogtimes.skywars.spigot.arena.chest.ChestType;
import fun.ogtimes.skywars.spigot.arena.chest.ChestTypeManager;
import fun.ogtimes.skywars.spigot.arena.event.ArenaEvent;
import fun.ogtimes.skywars.spigot.arena.event.ArenaEventManager;
import fun.ogtimes.skywars.spigot.config.ConfigManager;
import fun.ogtimes.skywars.spigot.config.SkyConfiguration;
import fun.ogtimes.skywars.spigot.events.ArenaFinishEvent;
import fun.ogtimes.skywars.spigot.events.ArenaJoinEvent;
import fun.ogtimes.skywars.spigot.events.ArenaLeaveEvent;
import fun.ogtimes.skywars.spigot.events.ArenaTickEvent;
import fun.ogtimes.skywars.spigot.events.SkySignUpdateEvent;
import fun.ogtimes.skywars.spigot.events.enums.ArenaJoinCause;
import fun.ogtimes.skywars.spigot.events.enums.ArenaLeaveCause;
import fun.ogtimes.skywars.spigot.events.enums.SkySignUpdateCause;
import fun.ogtimes.skywars.spigot.events.enums.SpectatorReason;
import fun.ogtimes.skywars.spigot.kit.Kit;
import fun.ogtimes.skywars.spigot.player.SkyPlayer;
import fun.ogtimes.skywars.spigot.utils.*;
import fun.ogtimes.skywars.spigot.utils.economy.SkyEconomyManager;
import fun.ogtimes.skywars.spigot.utils.title.Title;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

@Getter @Setter
public class Arena extends Game {
    private final List<SkyPlayer> players = new ArrayList<>();
    private final LinkedHashMap<Location, Boolean> spawnPoints = new LinkedHashMap<>();
    private final List<ArenaBox> glassBoxes = new ArrayList<>();
    private final List<String> selectedChest = new ArrayList<>();
    private final List<String> selectedTime = new ArrayList<>();
    private final List<Location> dontFill = new ArrayList<>();
    private final List<Location> originalChestLocations = new ArrayList<>();
    private final List<BukkitRunnable> tickers = new ArrayList<>();
    private final List<Integer> startingCounts = new ArrayList<>();
    private final HashMap<Integer, ArenaTeam> teams = new HashMap<>();
    private int minPlayers;
    private boolean forceStart;
    private boolean fallDamage;
    private boolean abilities;
    private boolean chestSelected;
    private boolean hardReset;
    private boolean disabled;
    private int startCountdown;
    private int startFullCountdown;
    private int endCountdown;
    private int maxTimeCountdown;
    private ArenaMode mode;
    private SkyConfiguration config;
    private List<Location> chestFilled = new ArrayList<>();
    private LinkedList<ArenaEvent> events = new LinkedList<>();
    private String chest = "";
    private HashMap<SkyPlayer, Integer> killStreak = new HashMap<>();
    private BukkitRunnable ticks;
    private HashMap<SkyPlayer, ArenaTeam> playerTeam = new HashMap<>();
    private Location teamLobby;
    private int teamCountdown;
    private int loadWorldTries = 0;

    public Arena(String name) {
        super(name, name, 0, false, ArenaState.WAITING);
        this.createConfig(name);
        this.forceStart = false;
        this.fallDamage = true;
        this.abilities = this.config.getBoolean("options.abilities");
        this.chestSelected = false;
        this.hardReset = false;
        this.displayName = this.config.getString("name");
        if (this.getWorld() == null) {
            this.loadFirstWorld();
        }

        if (this.config.getString("options.mode").equalsIgnoreCase("TEAM")) {
            this.mode = ArenaMode.SOLO;
        } else {
            this.mode = ArenaMode.SOLO;
        }

        if (this.mode == ArenaMode.SOLO) {
            this.minPlayers = this.config.getInt("min_players");
            this.maxPlayers = this.config.getInt("max_players");
            this.loadSpawnPoints();
            this.loadGlassBoxes();
        } else {
            for(int var2 = 1; var2 <= this.config.getInt("team.teams"); ++var2) {
                this.teams.put(var2, new ArenaTeam(var2, this.config.getInt("team.teams_size"), LocationUtil.getLocation(this.config.getStringList("spawnpoints").get(var2 - 1))));
            }

            this.minPlayers = this.config.getInt("min_players");
            this.maxPlayers = this.teams.size() * this.config.getInt("team.teams_size");
            String var7 = this.config.getString("team.waiting_lobby");
            if (var7 != null && !var7.isEmpty()) {
                this.teamLobby = LocationUtil.getLocation(this.config.getString("team.waiting_lobby"));
            } else {
                this.teamLobby = this.getSpawn();
            }

            this.teamCountdown = this.config.getInt("team.start_countdown");
        }

        this.startCountdown = this.config.getInt("countdown.starting");
        this.startFullCountdown = this.config.getInt("countdown.starting_full");
        this.endCountdown = this.config.getInt("countdown.end");
        this.maxTimeCountdown = ConfigManager.main.getInt("maxtime");
        String[] var8 = this.config.getString("countdown.starting_message").split(",");
        int var3 = var8.length;

        int var4;
        for(var4 = 0; var4 < var3; ++var4) {
            String var5 = var8[var4];
            int var6 = Integer.parseInt(var5);
            this.startingCounts.add(var6);
        }

        this.clearData();
        ChestType[] var9 = ChestTypeManager.getChestTypes();
        var3 = var9.length;

        for(var4 = 0; var4 < var3; ++var4) {
            ChestType var11 = var9[var4];
            this.addData("vote_chest_" + var11.getName(), 0);
        }

        this.addData("vote_time_day", 0);
        this.addData("vote_time_night", 0);
        this.addData("vote_time_sunset", 0);
        ArenaEvent[] var10 = ArenaEventManager.getArenaEvents(this);
        var3 = var10.length;

        for(var4 = 0; var4 < var3; ++var4) {
            ArenaEvent var12 = var10[var4];
            this.events.add(var12);
        }

        ArenaManager.games.put(name, this);
        this.startTicks();
    }

    public Arena(String var1, boolean var2) {
        super(var1, var1, 0, true, ArenaState.WAITING);
        this.disabled = var2;
        this.createConfig(var1);
        if (this.getWorld() == null) {
            this.loadFirstWorld();
        }

        ArenaManager.games.put(var1, this);
        this.playSignUpdate(SkySignUpdateCause.ALL);
    }

    public void addFilled(Location var1) {
        if (!this.chestFilled.contains(var1)) {
            this.chestFilled.add(var1);
        }

    }

    public void removeFilled(Location var1) {
        this.chestFilled.remove(var1);

    }

    public void addPlayer(SkyPlayer var1, ArenaJoinCause var2) {
        if (null == var1) {
            SkyWars.log("Arena.addPlayer - Trying to add a NULL Player");
        } else if (this.disabled) {
            if (var1.getPlayer().hasPermission("skywars.admin")) {
                var1.teleport(this.getWorld().getSpawnLocation());
            } else if (SkyWars.isProxyMode()) {
                var1.getPlayer().kickPlayer("You don't have permissions to enter to edit this game");
            } else {
                var1.sendMessage("&cYou don't have permissions to enter to edit this game");
            }

        } else if (this.isLoading()) {
            SkyWars.log("Arena.addPlayer - Trying to join Player when game is Reloading");
            if (SkyWars.isProxyMode()) {
                var1.getPlayer().kickPlayer(SkyWars.getMessage(Messages.GAME_LOADING));
            } else {
                var1.sendMessage(SkyWars.getMessage(Messages.GAME_LOADING));
            }

        } else {
            if (!var1.getPlayer().hasPermission("skywars.admin.spectate")) {
                if (this.state == ArenaState.INGAME) {
                    if (SkyWars.isProxyMode()) {
                        var1.getPlayer().kickPlayer(SkyWars.getMessage(Messages.GAME_INGAME_MESSAGE));
                    } else {
                        var1.sendMessage(SkyWars.getMessage(Messages.GAME_INGAME_MESSAGE));
                    }

                    return;
                }

                if (this.getAlivePlayers() >= this.maxPlayers) {
                    if (SkyWars.isProxyMode()) {
                        var1.getPlayer().kickPlayer(SkyWars.getMessage(Messages.GAME_FULL_MESSAGE));
                    } else {
                        var1.sendMessage(SkyWars.getMessage(Messages.GAME_FULL_MESSAGE));
                    }

                    return;
                }
            }

            ArenaJoinEvent var3 = new ArenaJoinEvent(var1, this, var2);
            Bukkit.getServer().getPluginManager().callEvent(var3);
            this.playSignUpdate(SkySignUpdateCause.PLAYERS);
        }
    }

    public void addTimer(BukkitRunnable var1, long var2, long var4) {
        this.tickers.add(var1);
        var1.runTaskTimer(SkyWars.getPlugin(), var2, var4);
    }

    public List<BukkitRunnable> getTimers() {
        return this.tickers;
    }

    public void broadcast(String var1) {

        for (SkyPlayer var3 : this.players) {
            if (var3.getPlayer() != null && var3.getPlayer().isOnline()) {
                var3.sendMessage(var1);
            }
        }

    }

    private void createConfig(String var1) {
        File configFile = new File(SkyWars.getPlugin().getDataFolder(), SkyWars.arenas + File.separator + var1 + ".yml");
        this.config = new SkyConfiguration(configFile);
        this.config.addDefault("name", var1, "name displayed in the server");
        this.config.addDefault("min_players", 2, "players required to start the game");
        this.config.addDefault("max_players", 6, "maximum amount of players that can join to the arena");
        this.config.addDefault("spawnpoints", new ArrayList<>(), "spawn where the player will spawn in the game (boxes)");
        this.config.addDefault("spectator_spawn", "", "spawn where spectators will appear");
        this.config.addDefault("countdown.starting", 90, "time in seconds to start the game");
        this.config.addDefault("countdown.starting_message", "90,60,30,10,5,4,3,2,1", "list of seconds when the time message will be displayed");
        this.config.addDefault("countdown.starting_full", 10, "if the game is full or can't start due to not required players the countdown will be this value");
        this.config.addDefault("countdown.end", 10, "time in seconds for the duration of the end (for win effects)");
        this.config.addDefault("options.abilities", true, "enable or disable abilities in this arena");
        this.config.addDefault("options.mode", ArenaMode.SOLO.toString(), "mode to be played in this arena", "Available modes: SOLO");
        this.config.addDefault("options.events", true, "enable or disable arena events");
        this.config.addDefault("options.vote.chest", true, "enable or disable chest vote");
        this.config.addDefault("options.vote.time", true, "enable or disable time vote");
        String var2 = ChestTypeManager.getChestType("Normal").getName();
        if (var2 == null || var2.isEmpty()) {
            var2 = ChestTypeManager.getChestTypes()[0].getName();
        }

        this.config.addDefault("chests.default", var2, "chest type that will be selected by default in this arena");
        ArrayList var3 = new ArrayList();
        ChestType[] var4 = ChestTypeManager.getChestTypes();

        for (ChestType var7 : var4) {
            var3.add(var7.getName());
        }

        this.config.addDefault("chests.selectable", var3, "list of chest types that can be selected to vote in this arena");
        ArrayList var8 = new ArrayList();
        var8.add("REFILL:" + var2 + ",300,Supply refill");
        this.config.addDefault("events", var8, "list of events to be executed in game in the list order", "Available events:", "    REFILL - Argument: Chest Type (Example: Overpowered) also, can be \"Selected\" to refill with the selected chest type", "Usage format: EVENT:Argument,Seconds,Title", "    Argument: The argument of the event, if not the event will take the default or a random argument", "    Seconds: Time in seconds to be executed the event after the game start or from the previous event", "    Title: The name of the event that will be displayed in game", "Example format: REFILL:Normal,300,Refill event");
        this.config.options().copyDefaults(true);
        this.config.getEConfig().setNewLinePerKey(true);
        this.config.save();
    }

    public void clearItems() {

        for (Entity var2 : this.getWorld().getEntities()) {
            if (var2 instanceof Item) {
                var2.remove();
            }
        }

    }

    public void clearMobs() {
        Iterator var1 = this.getWorld().getEntities().iterator();

        while(true) {
            Entity var2;
            do {
                if (!var1.hasNext()) {
                    return;
                }

                var2 = (Entity)var1.next();
            } while(!(var2 instanceof Animals) && !(var2 instanceof Monster));

            var2.remove();
        }
    }

    public void end(boolean countdown) {
        this.state = ArenaState.ENDING;
        this.playSignUpdate(SkySignUpdateCause.STATE);

        for (Player player : this.getWorld().getPlayers()) {
            if (player.isDead()) {
                (new BukkitRunnable() {
                    public void run() {
                        player.spigot().respawn();
                    }
                }).runTaskLater(SkyWars.getPlugin(), 10L);
            }
        }

        if (countdown) {
            this.endCountdown = 2;
        }

    }

    public void end(final SkyPlayer winner) {
        if (this.getState() != ArenaState.ENDING) {
            this.clearItems();
            this.broadcast(String.format(SkyWars.getMessage(Messages.GAME_FINISH_BROADCAST_WINNER), winner.getName(), this.name));
            SkyEconomyManager.addCoins(winner.getPlayer(), SkyWars.getPlugin().getConfig().getInt("reward.win"), true);
            this.executeWinnerCommands(ConfigManager.main.getBoolean("reward.wincmd.enabled"), winner);
            winner.addWins(1);
            winner.clearInventory(false);

            this.addTimer(new BukkitRunnable() {

                public void run() {
                    if (winner.getPlayer() != null && winner.getPlayer().getWorld() != null && winner.getPlayer().getWorld().equals(Arena.this.getWorld())) {
                        Arena.this.launchFirework(winner);
                    }

                }
            }, 0L, 10L);

            this.end(false);

            Audience killedAudience = SkyWars.getPlugin().getAdventure().player(winner.getPlayer());
            Component secondLine = Utils.component(SkyWars.getMessage(Messages.PLAY_AGAIN_2))
                    .replaceText(TextReplacementConfig.builder()
                            .match("<again>")
                            .replacement(Utils.component(SkyWars.getMessage(Messages.AGAIN))
                                    .clickEvent(ClickEvent.runCommand("/playagain"))
                            )
                            .build()
                    )
                    .replaceText(TextReplacementConfig.builder()
                            .match("<leave>")
                            .replacement(Utils.component(SkyWars.getMessage(Messages.LEAVE))
                                    .clickEvent(ClickEvent.runCommand("/salir"))
                            )
                            .build()
                    );

            winner.sendMessage("        &m----------------------------------");
            winner.sendMessage(SkyWars.getMessage(Messages.PLAY_AGAIN_1));
            killedAudience.sendMessage(secondLine);
            winner.sendMessage("        &m----------------------------------");

            Bukkit.getPluginManager().callEvent(new ArenaFinishEvent(this, winner));
        }
    }

    public void executeWinnerCommands(boolean var1, SkyPlayer var2) {
        if (var1) {

            for (String var4 : ConfigManager.main.getStringList("reward.wincmd.list")) {
                String[] var5 = var4.split("/");
                int var6 = Integer.parseInt(var5[0]);
                String var7 = var5[1].replace("%winner%", var2.getName()).replace("%map%", this.getName());
                if (this.getChance() < (double) var6) {
                    SkyWars.getPlugin().getServer().dispatchCommand(SkyWars.getPlugin().getServer().getConsoleSender(), var7);
                }
            }
        }

    }

    public List<SkyPlayer> getAlivePlayer() {
        ArrayList var1 = new ArrayList();

        for (SkyPlayer var3 : this.players) {
            if (!var3.isSpectating()) {
                var1.add(var3);
            }
        }

        return var1;
    }

    public int getAlivePlayers() {
        int var1 = 0;
        Iterator var2 = this.players.iterator();

        while(true) {
            while(true) {
                SkyPlayer skyPlayer;
                Player player;
                do {
                    do {
                        if (!var2.hasNext()) {
                            return var1;
                        }

                        skyPlayer = (SkyPlayer)var2.next();
                    } while(skyPlayer.isSpectating());

                    player = skyPlayer.getPlayer();
                } while(player == null);

                if (player.getGameMode() == GameMode.SPECTATOR) {
                    skyPlayer.setSpectating(true, SpectatorReason.DEATH);
                } else {
                    ++var1;
                }
            }
        }
    }

    public ArenaMode getArenaMode() {
        return this.mode;
    }

    public int getAvailableSlots() {
        return this.getMaxPlayers() - this.getAlivePlayers();
    }

    private double getChance() {
        return Math.random() * 100.0D;
    }

    public String getChest() {
        return this.getSelectedChest();
    }

    public List<ArenaBox> getGlassBoxes() {
        if (this.mode != ArenaMode.TEAM) {
            return this.glassBoxes;
        } else {
            ArrayList var1 = new ArrayList();

            for (ArenaTeam var3 : this.teams.values()) {
                var1.addAll(var3.getCages());
            }

            return var1;
        }
    }

    public String getSelectedChest() {
        if (this.chestSelected) {
            return this.chest;
        } else {
            this.selectedChest.clear();
            ChestType[] chestTypes = ChestTypeManager.getChestTypes();

            for (ChestType chestType : chestTypes) {
                this.selectedChest.add("vote_chest_" + chestType.getName());
            }

            int var5 = -1;
            String chest = null;

            for (String stringChest : this.selectedChest) {
                if (this.getInt(stringChest) > var5) {
                    var5 = this.getInt(stringChest);
                    chest = stringChest.replace("vote_chest_", "");
                }
            }

            if (var5 <= 0) {
                chest = this.config.getString("chests.default");
            }

            this.chest = chest;
            this.chestSelected = true;
            return chest;
        }
    }

    public String getSelectedTime() {
        this.selectedTime.clear();
        this.selectedTime.add("vote_time_day");
        this.selectedTime.add("vote_time_night");
        this.selectedTime.add("vote_time_sunset");
        int var1 = -1;
        String var2 = null;

        for (String var4 : this.selectedTime) {
            if (this.getInt(var4) > var1) {
                var1 = this.getInt(var4);
                var2 = var4.split("_")[2];
            }
        }

        if (var1 <= 0) {
            return "default";
        } else {
            return var2;
        }
    }

    public final Location getSpawn() {
        Location var1 = null;
        if (this.hasSpectSpawn()) {
            var1 = LocationUtil.getLocation(this.config.getString("spectator_spawn"));
        } else {
            try {
                throw new IllegalAccessException("Spectator spawn from (" + this.getName() + ") hasn't been found");
            } catch (IllegalAccessException var3) {
                var3.printStackTrace();
            }
        }

        return var1 != null ? var1 : this.getWorld().getSpawnLocation();
    }

    public Location getSpawnPoint() {
        if (SkyWars.getPlugin().getConfig().getBoolean("options.orderedSpawnPoints")) {

            for (Location location : this.spawnPoints.keySet()) {
                if (!this.spawnPoints.get(location)) {
                    return location;
                }
            }
        } else {
            List<Location> spawnpoints = new ArrayList<>(this.spawnPoints.keySet());
            Collections.shuffle(spawnpoints);

            for (Location spawnpoint : spawnpoints) {
                if (!this.spawnPoints.get(spawnpoint)) {
                    return spawnpoint;
                }
            }
        }

        return null;
    }

    public long getTime() {
        String var1 = this.getSelectedTime();
        if (var1.equalsIgnoreCase("day")) {
            return 0L;
        } else if (var1.equalsIgnoreCase("night")) {
            return 18000L;
        } else {
            return var1.equalsIgnoreCase("sunset") ? 12000L : 24000L;
        }
    }

    public final World getWorld() {
        return Bukkit.getWorld(this.name);
    }

    public int getKillStreak(SkyPlayer var1) {
        return this.killStreak.containsKey(var1) ? this.killStreak.get(var1) : 0;
    }

    public void addKillStreak(SkyPlayer var1) {
        if (this.killStreak.containsKey(var1)) {
            this.killStreak.put(var1, this.killStreak.get(var1) + 1);
        } else {
            this.killStreak.put(var1, 1);
        }

    }

    public void goToSpawn(SkyPlayer var1) {
        var1.teleport(this.getSpawn());
    }

    public boolean hasSpectSpawn() {
        return this.config.getString("spectator_spawn") != null || !this.config.getString("spectator_spawn").isEmpty();
    }

    public boolean isAbilitiesEnabled() {
        return this.abilities;
    }

    public boolean isFilled(Location var1) {
        return this.chestFilled.contains(var1);
    }

    public void setForceStart(boolean var1) {
        this.forceStart = var1;
    }

    public boolean isFull() {
        return this.players.size() >= this.getMaxPlayers();
    }

    public boolean isUsed(Location var1) {
        return this.spawnPoints.get(var1);
    }

    public void launchFirework(SkyPlayer skyPlayer) {
        Location location = skyPlayer.getPlayer().getLocation();
        RandomFirework.launchRandomFirework(location);
    }

    public final void loadGlassBoxes() {
        this.glassBoxes.clear();

        for (Location var2 : this.spawnPoints.keySet()) {
            ArenaBox box = new ArenaBox(var2);
            box.setBox(SkyWars.boxes.getInt("boxes." + SkyWars.boxes.getString("default") + ".item"), SkyWars.boxes.getInt("boxes." + SkyWars.boxes.getString("default") + ".data"));
            this.glassBoxes.add(box);
        }

    }

    public final void loadSpawnPoints() {
        this.spawnPoints.clear();

        for (Object spawnpoint : this.config.getList("spawnpoints")) {
            this.spawnPoints.put(LocationUtil.getLocation(spawnpoint.toString()), false);
        }

    }

    public final World loadFirstWorld() {
        if (this.getWorld() != null) {
            if (!Bukkit.unloadWorld(this.getWorld(), false)) {
                Console.debugWarn(this.name + " is already loaded but SkyWars is trying to unload the world for resetting (something is keeping the world loaded)");
                ++this.loadWorldTries;
                if (this.loadWorldTries >= 10) {
                    SkyWars.logError(this.name + " can not be unloaded, SkyWars tried 10 times but another instance keep the world loaded");
                    this.loadWorldTries = 0;
                    return this.getWorld();
                }
            }

            return this.loadFirstWorld();
        } else {
            WorldCreator creator = new WorldCreator(this.name);
            creator.generateStructures(false);
            creator.generator(SkyWars.getVoidGenerator());
            World world = creator.createWorld();
            world.setAutoSave(false);
            world.setGameRuleValue("doMobSpawning", "false");
            world.setGameRuleValue("doDaylightCycle", "false");
            world.setGameRuleValue("commandBlockOutput", "false");
            world.setTime(0L);
            world.setDifficulty(Difficulty.NORMAL);

            try {
                world.setKeepSpawnInMemory(false);
            } catch (Exception ex) {
                SkyWars.logError("An error has occurred while trying to load the world: " + this.name);
                SkyWars.logError("Error message: " + ex.getMessage());
            }

            this.loadWorldTries = 0;
            return world;
        }
    }

    public void reloadWorld() {

        for (Player player : this.getWorld().getPlayers()) {
            if (SkyWars.isProxyMode()) {
                ProxyUtils.teleToServer(player, SkyWars.getMessage(Messages.PLAYER_TELEPORT_LOBBY), SkyWars.getRandomLobby());
            } else {
                SkyPlayer skyPlayer = SkyWars.getSkyPlayer(player);
                if (skyPlayer == null) {
                    player.kickPlayer("Do you have lag?\nWe need reset the world :)");
                } else {
                    SkyWars.goToSpawn(skyPlayer);
                    player.setFallDistance(0.0F);
                }
            }
        }

        if (!Bukkit.unloadWorld(this.getWorld(), false)) {
            SkyWars.logError(this.name + " was unsuccessful unloaded before the world reset (this can cause some problems and it's not a SkyWars problem)");
        }

        if (this.hardReset) {
            File mapsFolder = new File(SkyWars.maps);
            File[] mapsFiles = mapsFolder.listFiles();

            for (File mapFile : mapsFiles) {
                if (mapFile.getName().equals(this.getName()) && mapFile.isDirectory()) {
                    try {
                        ArenaManager.delete(new File(mapFile.getName()));
                        ArenaManager.copyFolder(mapFile, new File(mapFile.getName()));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }

        this.loadFirstWorld();
        this.loadSpawnPoints();
        this.loadGlassBoxes();
        this.loading = false;
        this.hardReset = false;
    }

    public void removePlayer(SkyPlayer skyPlayer, ArenaLeaveCause cause) {
        ArenaLeaveEvent var3 = new ArenaLeaveEvent(skyPlayer, this, cause);
        Bukkit.getServer().getPluginManager().callEvent(var3);
        if (cause != ArenaLeaveCause.RESTART) {
            this.playSignUpdate(SkySignUpdateCause.PLAYERS);
        }

    }

    public void removeTimer(BukkitRunnable runnable) {
        this.tickers.remove(runnable);
    }

    public void resetPlayer(SkyPlayer skyPlayer) {
        this.setUsed(skyPlayer.getArenaSpawn(), false);
        skyPlayer.playedTimeEnd();
        skyPlayer.distanceWalkedConvert();
        skyPlayer.setBox(null);
        skyPlayer.setArenaSpawn(null);
        skyPlayer.clearInventory(false);
        skyPlayer.resetInventory();
        skyPlayer.resetVotes();
        skyPlayer.setArena(null);
        skyPlayer.getPlayer().updateInventory();
    }

    public void restart() {
        Iterator<?> players = this.tickers.iterator();

        while(players.hasNext()) {
            BukkitRunnable var2 = (BukkitRunnable)players.next();
            var2.cancel();
        }

        this.tickers.clear();
        this.loading = true;
        this.state = ArenaState.WAITING;
        this.forceStart = false;
        this.fallDamage = true;
        this.chestSelected = false;
        this.clearData();
        ChestType[] chestTypes = ChestTypeManager.getChestTypes();

        for (ChestType chestType : chestTypes) {
            this.addData("vote_chest_" + chestType.getName(), 0);
        }

        this.addData("vote_time_day", 0);
        this.addData("vote_time_night", 0);
        this.addData("vote_time_sunset", 0);
        players = this.getWorld().getPlayers().iterator();

        while(players.hasNext()) {
            Player player = (Player) players.next();
            SkyPlayer skyPlayer = SkyWars.getSkyPlayer(player);
            if (this.getPlayers().contains(skyPlayer)) {
                if (!SkyWars.isProxyMode()) {
                    this.removePlayer(skyPlayer, ArenaLeaveCause.RESTART);
                }
            } else if (SkyWars.isProxyMode()) {
                ProxyUtils.teleToServer(player, SkyWars.getMessage(Messages.PLAYER_TELEPORT_LOBBY), SkyWars.getRandomLobby());
            } else {
                SkyWars.goToSpawn(skyPlayer);
                skyPlayer.getPlayer().setFallDistance(0.0F);
            }
        }

        this.players.clear();
        this.glassBoxes.clear();
        this.selectedChest.clear();
        this.selectedTime.clear();
        this.chestFilled.clear();
        this.dontFill.clear();
        this.originalChestLocations.clear();
        this.events.clear();
        this.killStreak.clear();
        this.events.addAll(Arrays.asList(ArenaEventManager.getArenaEvents(this)));
        this.startCountdown = this.config.getInt("countdown.starting");
        this.startFullCountdown = this.config.getInt("countdown.starting_full");
        this.endCountdown = this.config.getInt("countdown.end");
        this.maxTimeCountdown = ConfigManager.main.getInt("maxtime");
        this.reloadWorld();
        this.playSignUpdate(SkySignUpdateCause.ALL);
    }

    public void setForceStart() {
        if (!this.forceStart) {
            this.forceStart = true;
        }

    }

    public void setState(ArenaState var1) {
        this.state = var1;
        this.playSignUpdate(SkySignUpdateCause.STATE);
    }

    public void setUsed(Location var1, boolean var2) {
        this.spawnPoints.put(var1, var2);
    }

    public void start() {
        if (!ConfigManager.main.getBoolean("options.creaturespawn")) {
            this.clearMobs();
        }

        this.state = ArenaState.INGAME;
        if (this.mode == ArenaMode.SOLO) {
            this.startGo();
        } else {

            for (SkyPlayer skyPlayer : this.players) {
                this.setTeam(skyPlayer);
                ArenaTeam team = this.playerTeam.get(skyPlayer);
                Location location = team.getSpawnUsable();
                skyPlayer.teleport(location);
                skyPlayer.setArenaSpawn(location);

                for (ArenaBox box : team.getCages()) {
                    if (box.getLocation().equals(location)) {
                        skyPlayer.setBox(box);
                    }
                }

                String boxSection = skyPlayer.getBoxSection();
                if (skyPlayer.getBoxSection() != null && !boxSection.equalsIgnoreCase(SkyWars.boxes.getString("default"))) {
                    int boxItem;
                    int boxData;
                    ArenaBox box;
                    String var12;
                    if (skyPlayer.getBoxItem(skyPlayer.getBoxSection()) != 0) {
                        var12 = skyPlayer.getBoxSection();
                    } else {
                        skyPlayer.getPlayer().setMetadata("upload_me", new FixedMetadataValue(SkyWars.getPlugin(), true));
                        var12 = SkyWars.boxes.getString("default");
                        skyPlayer.setBoxSection(var12, true);
                    }
                    boxItem = skyPlayer.getBoxItem(var12);
                    boxData = skyPlayer.getBoxData(var12);
                    box = skyPlayer.getBox();
                    SkyWars.log("Arena.start - Box Section=" + var12 + ", Box Item=" + boxItem + ", Box Data=" + boxData + ", Box=" + box);
                    box.setBox(boxItem, boxData);
                }
            }

            BukkitRunnable runnable = new BukkitRunnable() {
                public void run() {
                    if (Arena.this.teamCountdown == 0) {
                        Arena.this.startGo();
                        this.cancel();
                    }

                    Arena.this.teamCountdown--;
                }
            };
            this.addTimer(runnable, 0L, 20L);
        }

        this.playSignUpdate(SkySignUpdateCause.STATE);
    }

    public void startGo() {
        this.broadcast(SkyWars.getMessage(Messages.GAME_START_GO_ALERT_CHAT));
        this.broadcast(SkyWars.getMessage(Messages.GAME_START_GO));

        for (ArenaBox arenaBox : this.getGlassBoxes()) {
            if (ConfigManager.main.getBoolean("options.removeAllCageOnStart")) {
                arenaBox.removeAll();
            } else {
                arenaBox.removeBase();
            }
        }

        this.fallDamage = false;
        this.broadcast(String.format(SkyWars.getMessage(Messages.SELECTED_CHEST), SkyWars.getMessage(Messages.valueOf("SELECTED_CHEST_" + this.getChest().toUpperCase()))));
        long time = this.getTime();
        if (time == 0L) {
            this.broadcast(String.format(SkyWars.getMessage(Messages.SELECTED_TIME), SkyWars.getMessage(Messages.SELECTED_TIME_DAY)));
        }

        if (time == 18000L) {
            this.broadcast(String.format(SkyWars.getMessage(Messages.SELECTED_TIME), SkyWars.getMessage(Messages.SELECTED_TIME_NIGHT)));
        }

        if (time == 12000L) {
            this.broadcast(String.format(SkyWars.getMessage(Messages.SELECTED_TIME), SkyWars.getMessage(Messages.SELECTED_TIME_SUNSET)));
        }

        if (time == 24000L) {
            this.broadcast(SkyWars.getMessage(Messages.SELECTED_TIME_DEFAULT));
        }

        this.getWorld().setTime(time);

        for (SkyPlayer skyPlayer : this.players) {
            Title title = new Title(SkyWars.getMessage(Messages.GAME_START_GO_ALERT), 10, 40, 20);
            title.send(skyPlayer.getPlayer());

            skyPlayer.getPlayer().getInventory().clear();
            skyPlayer.getPlayer().closeInventory();
            if (skyPlayer.hasKit()) {
                Kit kit = skyPlayer.getKit();

                for (ItemBuilder items : kit.getItems()) {
                    skyPlayer.getPlayer().getInventory().addItem(items.build());
                }
            }

            skyPlayer.resetVotes();
            skyPlayer.addPlayed(1);
            skyPlayer.playedTimeStart();
        }

    }

    private void playSignUpdate(SkySignUpdateCause cause) {
        Bukkit.getServer().getPluginManager().callEvent(new SkySignUpdateEvent(this.name, cause));
    }

    private void startTicks() {
        this.ticks = new BukkitRunnable() {
            public void run() {
                if (!Arena.this.disabled) {
                    Bukkit.getServer().getPluginManager().callEvent(new ArenaTickEvent(Arena.this));
                }

            }
        };
        this.ticks.runTaskTimer(SkyWars.getPlugin(), 0L, 20L);
    }

    private void setTeam(SkyPlayer skyPlayer) {
        if (!this.playerTeam.containsKey(skyPlayer)) {
            int teamsSize = this.config.getInt("team.teams_size");
            int teamNumber = 0;

            for (ArenaTeam team : this.teams.values()) {
                if (team.getPlayers().size() < teamsSize) {
                    teamsSize = team.getPlayers().size();
                    teamNumber = team.getNumber();
                }
            }

            ArenaTeam team = this.teams.get(teamNumber);
            if (team != null) {
                if (!team.getPlayers().contains(skyPlayer)) {
                    team.getPlayers().add(skyPlayer);
                    this.playerTeam.put(skyPlayer, team);
                }
            }
        }
    }

}
