package fun.ogtimes.skywars.spigot.commands.acf;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import fun.ogtimes.skywars.spigot.SkyWars;
import fun.ogtimes.skywars.spigot.arena.Arena;
import fun.ogtimes.skywars.spigot.arena.ArenaManager;
import fun.ogtimes.skywars.spigot.arena.ArenaState;
import fun.ogtimes.skywars.spigot.arena.GameQueue;
import fun.ogtimes.skywars.spigot.config.ConfigManager;
import fun.ogtimes.skywars.spigot.events.enums.ArenaJoinCause;
import fun.ogtimes.skywars.spigot.menus.MenuListener;
import fun.ogtimes.skywars.spigot.player.SkyPlayer;
import fun.ogtimes.skywars.spigot.utils.*;
import fun.ogtimes.skywars.spigot.utils.sky.SkyHologram;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * This code was made by jsexp, in case of any unauthorized
 * use, at least please leave credits.
 * Find more about me @ my <a href="https://github.com/hardcorefactions">GitHub</a> :D
 * © 2025 - jsexp
 */
@CommandAlias("sw|skywars")
public class SkyWarsCommand extends BaseCommand {

    @Default
    @HelpCommand
    public void help(CommandSender sender) {
        showCommandHelp();
    }

    @Subcommand("join")
    @CommandPermission("skywars.join")
    @Syntax("[arena]")
    @CommandCompletion("@arenas")
    public void join(Player player, @Optional Arena arena) {
        if (SkyWars.isProxyMode()) return;
        SkyPlayer skyPlayer = SkyWars.getSkyPlayer(player);
        if (skyPlayer == null) {
            return;
        }

        if (arena == null) {
            if (!skyPlayer.isInArena()) {
                if (GameQueue.withoutGames()) {
                    GameQueue.addPlayer(skyPlayer);
                    skyPlayer.sendMessage("&cNo games available, you has been added to the queue");
                    return;
                }

                Game game = GameQueue.getJoinableGame();
                if (game == null) {
                    GameQueue.addPlayer(skyPlayer);
                    skyPlayer.sendMessage("&cNo games available, you has been added to the queue");
                    return;
                }

                if (SkyWars.isMultiArenaMode()) {
                    arena = (Arena)game;
                    arena.addPlayer(skyPlayer, ArenaJoinCause.COMMAND);
                } else if (SkyWars.isLobbyMode()) {
                    ProxyUtils.teleToServer(skyPlayer.getPlayer(), "", game.getName());
                }

            }
            return;
        }

        if (SkyWars.isMultiArenaMode() && !skyPlayer.isInArena()) {
            if (arena.getState() == ArenaState.INGAME && !player.hasPermission("skywars.admin.spectate")) {
                skyPlayer.sendMessage(SkyWars.getMessage(Messages.GAME_INGAME_MESSAGE));
                return;
            }

            if (arena.getAlivePlayers() >= arena.getMaxPlayers() && !player.hasPermission("skywars.admin.spectate")) {
                skyPlayer.sendMessage(SkyWars.getMessage(Messages.GAME_FULL_MESSAGE));
                return;
            }

            if (arena.isLoading()) {
                skyPlayer.sendMessage(SkyWars.getMessage(Messages.GAME_LOADING));
                return;
            }

            arena.addPlayer(skyPlayer, ArenaJoinCause.COMMAND);
        }
    }

    @Subcommand("forcestart")
    @CommandPermission("skywars.admin.forcestart")
    public void forceStart(Player player) {
        SkyPlayer skyPlayer = SkyWars.getSkyPlayer(player);
        if (skyPlayer == null) {
            return;
        }

        if (skyPlayer.isInArena()) {
            if (skyPlayer.getArena().getPlayers().size() <= 1) {
                skyPlayer.sendMessage("&cYou need at least two (2) players to force the game");
                return;
            }

            Arena arena = skyPlayer.getArena();
            arena.setForceStart();
            arena.broadcast(SkyWars.getMessage(Messages.GAME_FORCESTART));
        }
    }

    @Subcommand("lobbyspawn")
    @CommandPermission("skywars.admin.lobbyspawn")
    public void lobbySpawn(Player player) {
        SkyWars.getPlugin().getConfig().set("spawn", LocationUtil.getString(player.getLocation(), true));
        ConfigManager.main.set("spawn", LocationUtil.getString(player.getLocation(), true));
        ConfigManager.main.save();
        SkyWars.spawn = player.getLocation();
        player.sendMessage("§aLobby Spawn set");
    }

    @Subcommand("tp")
    @CommandPermission("skywars.admin.tp")
    @Syntax("<world>")
    @CommandCompletion("@worlds")
    public void tp(Player player, World world) {
        SkyPlayer skyPlayer = SkyWars.getSkyPlayer(player);
        if (skyPlayer == null) {
            return;
        }

        if (!SkyWars.isLobbyMode()) {
            skyPlayer.sendMessage("&cYou can only use this command in lobby mode");
            return;
        }

        player.teleport(world.getSpawnLocation());
        skyPlayer.sendMessage("&aTeleported to world &e" + world);
    }

    @Subcommand("arena")
    @CommandPermission("skywars.admin.arena")
    public static class ArenaCommand extends BaseCommand {

        @Subcommand("load")
        @CommandPermission("skywars.admin.arena.load")
        @Syntax("<arena>")
        @CommandCompletion("@arenas")
        public void load(CommandSender sender, String worldName) {
            File mapsFolder = new File(SkyWars.maps);

            if (!mapsFolder.exists() || !mapsFolder.isDirectory()) {
                sender.sendMessage("§cMaps folder not found.");
                return;
            }

            File[] mapCandidates = mapsFolder.listFiles();
            if (mapCandidates == null) {
                sender.sendMessage("§cMaps folder is empty.");
                return;
            }

            boolean loaded = false;
            for (File candidate : mapCandidates) {
                if (!candidate.isDirectory()) continue;
                if (!candidate.getName().contains(worldName)) continue;

                try {
                    ArenaManager.delete(new File(candidate.getName()));
                    ArenaManager.copyFolder(candidate, new File(candidate.getName()));

                    WorldCreator creator = new WorldCreator(worldName);
                    creator.generateStructures(false);

                    World world = creator.createWorld();
                    if (world != null) {
                        world.setAutoSave(false);
                        world.setKeepSpawnInMemory(false);
                        world.setGameRuleValue("doMobSpawning", "false");
                        world.setGameRuleValue("doDaylightCycle", "false");
                        world.setGameRuleValue("mobGriefing", "false");
                        world.setGameRuleValue("commandBlockOutput", "false");
                        world.setTime(0L);
                    }

                    sender.sendMessage("§a" + worldName + " loaded");
                    loaded = true;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            if (!loaded) {
                sender.sendMessage("§cWorld not found in maps folder: " + worldName);
            }
        }

        @Subcommand("create")
        @CommandPermission("skywars.admin.arena.create")
        public void create(CommandSender sender, String arenaName) {
            Arena existingArena = ArenaManager.getGame(arenaName);
            if (existingArena != null) {
                sender.sendMessage("§cThis arena already exists!");
                return;
            }

            new Arena(arenaName, true);
            sender.sendMessage("§a" + arenaName + " has been created");
        }

        @Subcommand("disable")
        @CommandPermission("skywars.admin.arena.disable")
        @Syntax("<arena>")
        @CommandCompletion("@arenas")
        public void disable(CommandSender sender, Arena arena) {
            if (arena.isDisabled()) {
                sender.sendMessage("§cThe arena is already disabled");
                return;
            }

            arena.setDisabled(true);
            arena.restart();
            sender.sendMessage("§a" + arena.getName() + " has been disabled and now you can edit it");
        }

        @Subcommand("reload")
        @CommandPermission("skywars.admin.arena.reload")
        @Syntax("<arena>")
        @CommandCompletion("@arenas")
        public void reload(CommandSender sender, Arena arena) {
            arena.setDisabled(false);
            arena.restart();
            sender.sendMessage("§a" + arena.getName() + " has been reloaded" + (arena.isDisabled() ? " §aand now is enabled" : ""));
        }

        @Subcommand("save")
        @CommandPermission("skywars.admin.arena.save")
        @Syntax("<arena>")
        @CommandCompletion("@arenas")
        public void save(CommandSender sender, Arena arena) {
            if (!arena.isDisabled()) {
                sender.sendMessage("§cYou can't save an arena if it is not disabled");
                return;
            }

            arena.getWorld().save();

            String mapPath = SkyWars.maps + File.separator + arena.getName();
            ZipDir.zipFile(mapPath);
            sender.sendMessage("§aBackup created for " + arena.getName());

            File mapFolder = new File(mapPath);
            ArenaManager.delete(mapFolder);

            try {
                ArenaManager.copyFolder(new File(arena.getName()), mapFolder);
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            sender.sendMessage("§a" + arena.getName() + " has been saved in maps folder");
        }

        @Subcommand("spawn")
        @CommandPermission("skywars.admin.arena.create")
        public static class SpawnCommand extends BaseCommand {

            @Subcommand("add")
            public void add(Player player) {
                Arena arena = getArenaFromCurrentWorld(player);
                if (arena == null) {
                    player.sendMessage("§cFirst you need create the arena (/sw arena create <name>)");
                    return;
                }

                if (!arena.isDisabled()) {
                    player.sendMessage("§cYou can't edit an arena if it is not disabled");
                    return;
                }

                List<String> spawnpoints = new ArrayList<>(arena.getConfig().getStringList("spawnpoints"));
                spawnpoints.add(LocationUtil.getString(player.getLocation(), true));
                arena.getConfig().set("spawnpoints", spawnpoints);
                arena.getConfig().save();
                player.sendMessage("§aSpawn added (" + spawnpoints.size() + ")");
            }

            @Subcommand("remove")
            public void remove(Player player, @Optional Integer index) {
                Arena arena = getArenaFromCurrentWorld(player);
                if (arena == null) {
                    player.sendMessage("§cFirst you need create the arena (/sw arena create <name>)");
                    return;
                }

                if (!arena.isDisabled()) {
                    player.sendMessage("§cYou can't edit an arena if it is not disabled");
                    return;
                }

                List<String> spawnpoints = new ArrayList<>(arena.getConfig().getStringList("spawnpoints"));
                if (spawnpoints.isEmpty()) {
                    player.sendMessage("§cThis arena don't have spawn points");
                    return;
                }

                int indexToRemove = index == null ? spawnpoints.size() : index;
                if (indexToRemove < 1 || indexToRemove > spawnpoints.size()) {
                    player.sendMessage("§cInvalid spawn index. Choose between 1 and " + spawnpoints.size() + ".");
                    return;
                }

                spawnpoints.remove(indexToRemove - 1);
                arena.getConfig().set("spawnpoints", spawnpoints);
                arena.getConfig().save();
                player.sendMessage("§aSpawn #" + indexToRemove + " removed");
            }

            @Subcommand("spectator|spect")
            public void spectator(Player player, String arenaName) {
                Arena arena = getArenaFromCurrentWorld(player);
                if (arena == null) {
                    player.sendMessage("§cFirst you need create the arena (/sw arena create <name>)");
                    return;
                }

                if (!arena.isDisabled()) {
                    player.sendMessage("§cYou can't edit an arena if it is not disabled");
                    return;
                }

                arena.getConfig().set("spectator_spawn", LocationUtil.getString(player.getLocation(), true));
                arena.getConfig().save();
                player.sendMessage("§aSpectator spawn set");
            }

            private Arena getArenaFromCurrentWorld(Player player) {
                String currentWorldName = player.getWorld().getName();
                return ArenaManager.getGame(currentWorldName);
            }

        }

        @Subcommand("set")
        @CommandPermission("skywars.admin.arena.set")
        public static class SetCommand extends BaseCommand {

            @Subcommand("max")
            public void add(Player player, int maxPlayers) {
                Arena arena = getArenaFromCurrentWorld(player);
                if (arena == null) {
                    player.sendMessage("§cFirst you need create the arena (/sw arena create <name>)");
                    return;
                }

                if (!arena.isDisabled()) {
                    player.sendMessage("§cYou can't edit an arena if it is not disabled");
                    return;
                }

                arena.getConfig().set("max_players", maxPlayers);
                arena.getConfig().save();
                player.sendMessage("§aMaximum players set to " + maxPlayers + " in " + arena.getName());
            }

            @Subcommand("min")
            public void min(Player player, int minPlayers) {
                Arena arena = getArenaFromCurrentWorld(player);
                if (arena == null) {
                    player.sendMessage("§cFirst you need create the arena (/sw arena create <name>)");
                    return;
                }

                if (!arena.isDisabled()) {
                    player.sendMessage("§cYou can't edit an arena if it is not disabled");
                    return;
                }

                if (minPlayers <= 1) {
                    player.sendMessage("§cThere isn't recommended set minimum player to " + minPlayers + ", this could cause the game start after one player join the match (and if is alone will win)");
                }

                arena.getConfig().set("min_players", minPlayers);
                arena.getConfig().save();
                player.sendMessage("§aMinimun players set to " + minPlayers + " in " + arena.getName());
            }

            private Arena getArenaFromCurrentWorld(Player player) {
                String currentWorldName = player.getWorld().getName();
                return ArenaManager.getGame(currentWorldName);
            }

        }

    }

    @Subcommand("reload")
    @CommandPermission("skywars.admin.reload")
    public static class ReloadCommand extends BaseCommand {

        @Subcommand("all")
        public void all(CommandSender sender) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a" + SkyWars.prefix + "&eReloading all files"));

            reloadConfig(sender, false);
            reloadMessages(sender, false);
            reloadScoreboard(sender, false);
            reloadShops(sender, false);

            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a" + SkyWars.prefix + "All files reloaded"));
        }

        @Subcommand("config")
        public void config(CommandSender sender) {
            reloadConfig(sender, true);
        }

        @Subcommand("messages")
        public void messages(CommandSender sender) {
            reloadMessages(sender, true);
        }

        @Subcommand("scoreboard")
        public void scoreboard(CommandSender sender) {
            reloadScoreboard(sender, true);
        }

        @Subcommand("shops")
        public void shops(CommandSender sender) {
            reloadShops(sender, true);
        }

        private void reloadConfig(CommandSender sender, boolean sendMessage) {
            if (sendMessage) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a" + SkyWars.prefix + "&eReloading config file"));
            }

            try {
                SkyWars.reloadConfigMain();
                if (sendMessage) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a" + SkyWars.prefix + "Config file reloaded"));
                }
            } catch (Exception var4) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c" + SkyWars.prefix + "An error occurred while try to reload config file, please check console log"));
                SkyWars.getPlugin().getLogger().log(Level.SEVERE, "An error occurred in config.yml", var4);
            }

        }

        private void reloadMessages(CommandSender sender, boolean sendMessage) {
            if (sendMessage) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a" + SkyWars.prefix + "&eReloading messages files"));
            }

            try {
                SkyWars.reloadMessages();
                if (sendMessage) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a" + SkyWars.prefix + "Messages files reloaded"));
                }
            } catch (Exception var4) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c" + SkyWars.prefix + "An error occurred while try to reload messages files, please check console log"));
                SkyWars.getPlugin().getLogger().log(Level.SEVERE, "An error occurred in some message file", var4);
            }

        }

        private void reloadScoreboard(CommandSender sender, boolean sendMessage) {
            if (sendMessage) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a" + SkyWars.prefix + "&eReloading scoreboard file"));
            }

            try {
                SkyWars.reloadConfigScoreboard();
                if (sendMessage) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a" + SkyWars.prefix + "Scoreboard file reloaded"));
                }
            } catch (Exception var4) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c" + SkyWars.prefix + "An error occurred while try to reload scoreboard file, please check console log"));
                SkyWars.getPlugin().getLogger().log(Level.SEVERE, "An error occurred in scoreboard.yml", var4);
            }

        }

        private void reloadShops(CommandSender sender, boolean sendMessage) {
            if (sendMessage) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a" + SkyWars.prefix + "&eReloading shop file"));
            }

            try {
                SkyWars.reloadConfigShop();
                if (sendMessage) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a" + SkyWars.prefix + "Shop file reloaded"));
                }
            } catch (Exception var6) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c" + SkyWars.prefix + "An error occurred while try to reload shop file, please check console log"));
                SkyWars.getPlugin().getLogger().log(Level.SEVERE, "An error occurred in shop.yml", var6);
            }

            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a" + SkyWars.prefix + "&eReloading abilities file"));

            try {
                SkyWars.reloadConfigAbilities();
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a" + SkyWars.prefix + "Abilities file reloaded"));
            } catch (Exception var5) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c" + SkyWars.prefix + "An error occurred while try to reload abilities file, please check console log"));
                SkyWars.getPlugin().getLogger().log(Level.SEVERE, "An error occurred in abilities.yml", var5);
            }

            try {
                SkyWars.reloadAbilities();
            } catch (Exception var4) {
                SkyWars.getPlugin().getLogger().log(Level.SEVERE, "An error occurred while try to reload abilities objects", var4);
            }

        }

    }

    @Subcommand("open")
    @CommandPermission("skywars.cmd.open")
    public static class OpenCommand extends BaseCommand {

        @Subcommand("lshop")
        @CommandPermission("skywars.cmd.open.lshop")
        public void lshop(Player player) {
            SkyPlayer skyPlayer = SkyWars.getSkyPlayer(player);
            if (skyPlayer == null) {
                return;
            }

            if (SkyWars.isMultiArenaMode() && !skyPlayer.isInArena() || SkyWars.isLobbyMode()) {
                player.openInventory(MenuListener.getPlayerMenu(skyPlayer.getPlayer(), "shop").getInventory());
            }
        }

    }

    @Subcommand("hologram")
    @CommandPermission("skywars.admin.hologram")
    public static class HologramCommand extends BaseCommand {

        @Subcommand("add")
        @CommandPermission("skywars.admin.hologram.add")
        public void add(Player player) {
            List<String> locations = new ArrayList<>(ConfigManager.score.getStringList("hologram.locations"));
            locations.add(LocationUtil.getString(player.getLocation(), true));

            ConfigManager.score.set("hologram.locations", locations);
            ConfigManager.score.save();

            reloadHologramLocations();
            recreateHologramsInWorld(player.getWorld().getName());

            player.sendMessage("§aHologram added (" + locations.size() + ")");
        }

        @Subcommand("remove")
        @CommandPermission("skywars.admin.hologram.remove")
        @Syntax("<index>")
        public void remove(Player player, @Optional Integer index) {
            List<String> locations = new ArrayList<>(ConfigManager.score.getStringList("hologram.locations"));
            if (locations.isEmpty()) {
                player.sendMessage("§cThis server don't have hologram(s)");
                return;
            }

            int indexToRemove = index == null ? locations.size(): index;

            if (indexToRemove < 1 || indexToRemove > locations.size()) {
                player.sendMessage("§cInvalid hologram index. Choose between 1 and " + locations.size() + ".");
                return;
            }

            locations.remove(indexToRemove - 1);
            ConfigManager.score.set("hologram.locations", locations);
            ConfigManager.score.save();

            reloadHologramLocations();
            recreateHologramsInWorld(player.getWorld().getName());

            player.sendMessage("§aHologram #" + indexToRemove + " was removed");
        }

        private void reloadHologramLocations() {
            SkyWars.hologram.clear();
            for (String serializedLocation : ConfigManager.score.getStringList("hologram.locations")) {
                Location location = LocationUtil.getLocation(serializedLocation);
                if (location != null) {
                    SkyWars.hologram.add(location);
                }
            }
        }

        private void recreateHologramsInWorld(String worldName) {
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (online.getWorld() != null && online.getWorld().getName().equals(worldName)) {
                    SkyPlayer skyPlayer = SkyWars.getSkyPlayer(online);
                    SkyHologram.createHologram(skyPlayer);
                }
            }
        }

    }

}