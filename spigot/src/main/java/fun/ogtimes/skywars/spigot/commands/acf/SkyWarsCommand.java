package fun.ogtimes.skywars.spigot.commands.acf;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import fun.ogtimes.skywars.spigot.SkyWars;
import fun.ogtimes.skywars.spigot.arena.Arena;
import fun.ogtimes.skywars.spigot.arena.ArenaState;
import fun.ogtimes.skywars.spigot.arena.GameQueue;
import fun.ogtimes.skywars.spigot.config.ConfigManager;
import fun.ogtimes.skywars.spigot.events.enums.ArenaJoinCause;
import fun.ogtimes.skywars.spigot.menus.MenuListener;
import fun.ogtimes.skywars.spigot.player.SkyPlayer;
import fun.ogtimes.skywars.spigot.utils.Game;
import fun.ogtimes.skywars.spigot.utils.LocationUtil;
import fun.ogtimes.skywars.spigot.utils.Messages;
import fun.ogtimes.skywars.spigot.utils.ProxyUtils;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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

    }

}
