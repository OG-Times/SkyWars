package fun.ogtimes.skywars.spigot.commands.user;

import fun.ogtimes.skywars.spigot.SkyWars;
import fun.ogtimes.skywars.spigot.arena.Arena;
import fun.ogtimes.skywars.spigot.arena.ArenaManager;
import fun.ogtimes.skywars.spigot.arena.ArenaState;
import fun.ogtimes.skywars.spigot.arena.GameQueue;
import fun.ogtimes.skywars.spigot.commands.BaseCommand;
import fun.ogtimes.skywars.spigot.events.enums.ArenaJoinCause;
import fun.ogtimes.skywars.spigot.player.SkyPlayer;
import fun.ogtimes.skywars.spigot.utils.ProxyUtils;
import fun.ogtimes.skywars.spigot.utils.Game;
import fun.ogtimes.skywars.spigot.utils.Messages;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

public class CmdJoin implements BaseCommand {

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("You aren't a player!");
            return;
        }

        SkyPlayer skyPlayer = SkyWars.getSkyPlayer(player);
        if (skyPlayer == null) {
            return;
        }

        if (!player.hasPermission(this.getPermission())) {
            player.sendMessage("§cYou do not have permission!");
            return;
        }

        Arena arena;
        if (args.length == 0) {
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
        } else if (args.length == 1 && SkyWars.isMultiArenaMode() && !skyPlayer.isInArena()) {
            String var7 = args[0];
            arena = ArenaManager.getGame(var7);
            if (arena == null) {
                skyPlayer.sendMessage("&cThis arena doesn't exists");
                return;
            }

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

    public String help(CommandSender sender) {
        String var2 = "&a/sw join &e[ArenaName] &a- &bJoin to a random or specific";
        if (SkyWars.isLobbyMode()) {
            var2 = "&a/sw join &a- &bJoin to a random game";
        }

        return sender.hasPermission(this.getPermission()) ? var2 : "";
    }

    public String getPermission() {
        return "skywars.join";
    }

    public boolean console() {
        return false;
    }

    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (sender.hasPermission(this.getPermission()) && !SkyWars.isLobbyMode()) {
            if (args.length != 1) {
                return null;
            } else {
                ArrayList var3 = new ArrayList();
                ArrayList var4 = new ArrayList();
                Iterator var5 = ArenaManager.getGames().iterator();

                while(true) {
                    Arena var6;
                    do {
                        if (!var5.hasNext()) {
                            sender.sendMessage("--------------------------------------------");
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aAvailable games (&b" + var3.size() + "&a):"));
                            StringUtil.copyPartialMatches(args[0], var3, var4);
                            Collections.sort(var4);
                            return var4;
                        }

                        var6 = (Arena)var5.next();
                    } while(var6.getState() != ArenaState.WAITING && var6.getState() != ArenaState.STARTING);

                    if (var6.getAlivePlayers() < var6.getMaxPlayers()) {
                        var3.add(var6.getName());
                    }
                }
            }
        } else {
            return null;
        }
    }
}
