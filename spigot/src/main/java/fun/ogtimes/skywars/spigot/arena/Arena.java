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

public class Arena extends Game {
    @Getter
    private final List<SkyPlayer> players = new ArrayList<>();
    @Getter
    private final LinkedHashMap<Location, Boolean> spawnPoints = new LinkedHashMap<>();
    private final List<ArenaBox> glassBoxes = new ArrayList<>();
    private final List<String> selectedChest = new ArrayList<>();
    private final List<String> selectedTime = new ArrayList<>();
    @Getter
    private final List<Location> dontFill = new ArrayList<>();
    private final List<BukkitRunnable> tickers = new ArrayList<>();
    @Getter
    private final List<Integer> startingCounts = new ArrayList<>();
    private final HashMap<Integer, ArenaTeam> teams = new HashMap<>();
    @Getter
    private int minPlayers;
    @Getter
    private boolean forceStart;
    @Setter
    @Getter
    private boolean fallDamage;
    private boolean abilities;
    private boolean chestSelected;
    @Setter
    @Getter
    private boolean hardReset;
    @Setter
    @Getter
    private boolean disabled;
    @Setter
    @Getter
    private int startCountdown;
    @Setter
    @Getter
    private int startFullCountdown;
    @Setter
    @Getter
    private int endCountdown;
    @Setter
    @Getter
    private int maxTimeCountdown;
    private ArenaMode mode;
    @Getter
    private SkyConfiguration config;
    @Setter
    @Getter
    private List<Location> chestFilled = new ArrayList<>();
    @Setter
    @Getter
    private LinkedList<ArenaEvent> events = new LinkedList<>();
    private String chest = "";
    @Setter
    private HashMap<SkyPlayer, Integer> killStreak = new HashMap<>();
    @Getter
    private BukkitRunnable ticks;
    @Setter
    @Getter
    private HashMap<SkyPlayer, ArenaTeam> playerTeam = new HashMap<>();
    @Setter
    @Getter
    private Location teamLobby;
    private int teamCountdown;
    private int loadWorldTries = 0;

    public Arena(String var1) {
        super(var1, var1, 0, false, ArenaState.WAITING);
        this.createConfig(var1);
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

        ArenaManager.games.put(var1, this);
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
        int var5 = var4.length;

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
                SkyPlayer var3;
                Player var4;
                do {
                    do {
                        if (!var2.hasNext()) {
                            return var1;
                        }

                        var3 = (SkyPlayer)var2.next();
                    } while(var3.isSpectating());

                    var4 = var3.getPlayer();
                } while(var4 == null);

                if (SkyWars.is18orHigher() && var4.getGameMode() == GameMode.SPECTATOR) {
                    var3.setSpectating(true, SpectatorReason.DEATH);
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
            ChestType[] var1 = ChestTypeManager.getChestTypes();
            int var2 = var1.length;

            for (ChestType var4 : var1) {
                this.selectedChest.add("vote_chest_" + var4.getName());
            }

            int var5 = -1;
            String var6 = null;

            for (String var8 : this.selectedChest) {
                if (this.getInt(var8) > var5) {
                    var5 = this.getInt(var8);
                    var6 = var8.replace("vote_chest_", "");
                }
            }

            if (var5 <= 0) {
                var6 = this.config.getString("chests.default");
            }

            this.chest = var6;
            this.chestSelected = true;
            return var6;
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

            for (Location var2 : this.spawnPoints.keySet()) {
                if (!(Boolean) this.spawnPoints.get(var2)) {
                    return var2;
                }
            }
        } else {
            ArrayList var4 = new ArrayList(this.spawnPoints.keySet());
            Collections.shuffle(var4);

            for (Object object : var4) {
                Location var3 = (Location) object;
                if (!(Boolean) this.spawnPoints.get(var3)) {
                    return var3;
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

    public void launchFirework(SkyPlayer var1) {
        Location var2 = var1.getPlayer().getLocation();
        RandomFirework.launchRandomFirework(var2);
    }

    public final void loadGlassBoxes() {
        this.glassBoxes.clear();

        for (Location var2 : this.spawnPoints.keySet()) {
            ArenaBox var3 = new ArenaBox(var2);
            var3.setBox(SkyWars.boxes.getInt("boxes." + SkyWars.boxes.getString("default") + ".item"), SkyWars.boxes.getInt("boxes." + SkyWars.boxes.getString("default") + ".data"));
            this.glassBoxes.add(var3);
        }

    }

    public final void loadSpawnPoints() {
        this.spawnPoints.clear();

        for (Object var2 : this.config.getList("spawnpoints")) {
            this.spawnPoints.put(LocationUtil.getLocation(var2.toString()), false);
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
            WorldCreator var1 = new WorldCreator(this.name);
            var1.generateStructures(false);
            var1.generator(SkyWars.getVoidGenerator());
            World var2 = var1.createWorld();
            var2.setAutoSave(false);
            var2.setGameRuleValue("doMobSpawning", "false");
            var2.setGameRuleValue("doDaylightCycle", "false");
            var2.setGameRuleValue("commandBlockOutput", "false");
            var2.setTime(0L);
            var2.setDifficulty(Difficulty.NORMAL);

            try {
                var2.setKeepSpawnInMemory(false);
            } catch (Exception var4) {
                SkyWars.logError("An error has occurred while trying to load the world: " + this.name);
                SkyWars.logError("Error message: " + var4.getMessage());
            }

            this.loadWorldTries = 0;
            return var2;
        }
    }

    public void reloadWorld() {

        for (Player var2 : this.getWorld().getPlayers()) {
            if (SkyWars.isProxyMode()) {
                ProxyUtils.teleToServer(var2, SkyWars.getMessage(Messages.PLAYER_TELEPORT_LOBBY), SkyWars.getRandomLobby());
            } else {
                SkyPlayer var3 = SkyWars.getSkyPlayer(var2);
                if (var3 == null) {
                    var2.kickPlayer("Do you have lag?\nWe need reset the world :)");
                } else {
                    SkyWars.goToSpawn(var3);
                    var2.setFallDistance(0.0F);
                }
            }
        }

        if (!Bukkit.unloadWorld(this.getWorld(), false)) {
            SkyWars.logError(this.name + " was unsuccessful unloaded before the world reset (this can cause some problems and it's not a SkyWars problem)");
        }

        if (this.hardReset) {
            File var9 = new File(SkyWars.maps);
            File[] var10 = var9.listFiles();
            int var4 = var10.length;

            for (File var6 : var10) {
                if (var6.getName().equals(this.getName()) && var6.isDirectory()) {
                    try {
                        ArenaManager.delete(new File(var6.getName()));
                        ArenaManager.copyFolder(var6, new File(var6.getName()));
                    } catch (Exception var8) {
                        var8.printStackTrace();
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

    public void removePlayer(SkyPlayer var1, ArenaLeaveCause var2) {
        ArenaLeaveEvent var3 = new ArenaLeaveEvent(var1, this, var2);
        Bukkit.getServer().getPluginManager().callEvent(var3);
        if (var2 != ArenaLeaveCause.RESTART) {
            this.playSignUpdate(SkySignUpdateCause.PLAYERS);
        }

    }

    public void removeTimer(BukkitRunnable var1) {
        this.tickers.remove(var1);
    }

    public void resetPlayer(SkyPlayer var1) {
        this.setUsed(var1.getArenaSpawn(), false);
        var1.playedTimeEnd();
        var1.distanceWalkedConvert();
        var1.setBox(null);
        var1.setArenaSpawn(null);
        var1.clearInventory(false);
        var1.resetInventory();
        var1.resetVotes();
        var1.setArena(null);
        var1.getPlayer().updateInventory();
    }

    public void restart() {
        Iterator var1 = this.tickers.iterator();

        while(var1.hasNext()) {
            BukkitRunnable var2 = (BukkitRunnable)var1.next();
            var2.cancel();
        }

        this.tickers.clear();
        this.loading = true;
        this.state = ArenaState.WAITING;
        this.forceStart = false;
        this.fallDamage = true;
        this.chestSelected = false;
        this.clearData();
        ChestType[] var5 = ChestTypeManager.getChestTypes();
        int var6 = var5.length;

        for (ChestType var4 : var5) {
            this.addData("vote_chest_" + var4.getName(), 0);
        }

        this.addData("vote_time_day", 0);
        this.addData("vote_time_night", 0);
        this.addData("vote_time_sunset", 0);
        var1 = this.getWorld().getPlayers().iterator();

        while(var1.hasNext()) {
            Player var7 = (Player)var1.next();
            SkyPlayer var8 = SkyWars.getSkyPlayer(var7);
            if (this.getPlayers().contains(var8)) {
                if (!SkyWars.isProxyMode()) {
                    this.removePlayer(var8, ArenaLeaveCause.RESTART);
                }
            } else if (SkyWars.isProxyMode()) {
                ProxyUtils.teleToServer(var7, SkyWars.getMessage(Messages.PLAYER_TELEPORT_LOBBY), SkyWars.getRandomLobby());
            } else {
                SkyWars.goToSpawn(var8);
                var8.getPlayer().setFallDistance(0.0F);
            }
        }

        this.players.clear();
        this.glassBoxes.clear();
        this.selectedChest.clear();
        this.selectedTime.clear();
        this.chestFilled.clear();
        this.dontFill.clear();
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

            for (SkyPlayer var2 : this.players) {
                this.setTeam(var2);
                ArenaTeam var3 = this.playerTeam.get(var2);
                Location var4 = var3.getSpawnUsable();
                var2.teleport(var4);
                var2.setArenaSpawn(var4);

                for (ArenaBox var6 : var3.getCages()) {
                    if (var6.getLocation().equals(var4)) {
                        var2.setBox(var6);
                    }
                }

                String var11 = var2.getBoxSection();
                if (var2.getBoxSection() != null && !var11.equalsIgnoreCase(SkyWars.boxes.getString("default"))) {
                    int var7;
                    int var8;
                    ArenaBox var9;
                    String var12;
                    if (var2.getBoxItem(var2.getBoxSection()) != 0) {
                        var12 = var2.getBoxSection();
                        var7 = var2.getBoxItem(var12);
                        var8 = var2.getBoxData(var12);
                        var9 = var2.getBox();
                        SkyWars.log("Arena.start - Box Section=" + var12 + ", Box Item=" + var7 + ", Box Data=" + var8 + ", Box=" + var9);
                        var9.setBox(var7, var8);
                    } else {
                        var2.getPlayer().setMetadata("upload_me", new FixedMetadataValue(SkyWars.getPlugin(), true));
                        var12 = SkyWars.boxes.getString("default");
                        var2.setBoxSection(var12, true);
                        var7 = var2.getBoxItem(var12);
                        var8 = var2.getBoxData(var12);
                        var9 = var2.getBox();
                        SkyWars.log("Arena.start - Box Section=" + var12 + ", Box Item=" + var7 + ", Box Data=" + var8 + ", Box=" + var9);
                        var9.setBox(var7, var8);
                    }
                }
            }

            BukkitRunnable var10 = new BukkitRunnable() {
                public void run() {
                    if (Arena.this.teamCountdown == 0) {
                        Arena.this.startGo();
                        this.cancel();
                    }

                    Arena.this.teamCountdown--;
                }
            };
            this.addTimer(var10, 0L, 20L);
        }

        this.playSignUpdate(SkySignUpdateCause.STATE);
    }

    public void startGo() {
        this.broadcast(SkyWars.getMessage(Messages.GAME_START_GO_ALERT_CHAT));
        this.broadcast(SkyWars.getMessage(Messages.GAME_START_GO));

        for (ArenaBox var2 : this.getGlassBoxes()) {
            if (ConfigManager.main.getBoolean("options.removeAllCageOnStart")) {
                var2.removeAll();
            } else {
                var2.removeBase();
            }
        }

        this.fallDamage = false;
        this.broadcast(String.format(SkyWars.getMessage(Messages.SELECTED_CHEST), this.getChest()));
        long var8 = this.getTime();
        if (var8 == 0L) {
            this.broadcast(String.format(SkyWars.getMessage(Messages.SELECTED_TIME), SkyWars.getMessage(Messages.SELECTED_TIME_DAY)));
        }

        if (var8 == 18000L) {
            this.broadcast(String.format(SkyWars.getMessage(Messages.SELECTED_TIME), SkyWars.getMessage(Messages.SELECTED_TIME_NIGHT)));
        }

        if (var8 == 12000L) {
            this.broadcast(String.format(SkyWars.getMessage(Messages.SELECTED_TIME), SkyWars.getMessage(Messages.SELECTED_TIME_SUNSET)));
        }

        if (var8 == 24000L) {
            this.broadcast(SkyWars.getMessage(Messages.SELECTED_TIME_DEFAULT));
        }

        this.getWorld().setTime(var8);

        for (SkyPlayer var4 : this.players) {
            if (SkyWars.is18orHigher()) {
                Title var5 = new Title(SkyWars.getMessage(Messages.GAME_START_GO_ALERT), 10, 40, 20);
                var5.send(var4.getPlayer());
            }

            var4.getPlayer().getInventory().clear();
            var4.getPlayer().closeInventory();
            if (var4.hasKit()) {
                Kit var9 = var4.getKit();

                for (ItemBuilder var7 : var9.getItems()) {
                    var4.getPlayer().getInventory().addItem(var7.build());
                }
            }

            var4.resetVotes();
            var4.addPlayed(1);
            var4.playedTimeStart();
        }

    }

    private void playSignUpdate(SkySignUpdateCause var1) {
        Bukkit.getServer().getPluginManager().callEvent(new SkySignUpdateEvent(this.name, var1));
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

    private void setTeam(SkyPlayer var1) {
        if (!this.playerTeam.containsKey(var1)) {
            int var2 = this.config.getInt("team.teams_size");
            int var3 = 0;

            for (ArenaTeam var5 : this.teams.values()) {
                if (var5.getPlayers().size() < var2) {
                    var2 = var5.getPlayers().size();
                    var3 = var5.getNumber();
                }
            }

            ArenaTeam var6 = this.teams.get(var3);
            if (var6 != null) {
                if (!var6.getPlayers().contains(var1)) {
                    var6.getPlayers().add(var1);
                    this.playerTeam.put(var1, var6);
                }
            }
        }
    }

}
