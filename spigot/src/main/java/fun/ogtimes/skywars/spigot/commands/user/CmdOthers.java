package fun.ogtimes.skywars.spigot.commands.user;

import fun.ogtimes.skywars.spigot.SkyWars;
import fun.ogtimes.skywars.spigot.arena.Arena;
import fun.ogtimes.skywars.spigot.arena.GameQueue;
import fun.ogtimes.skywars.spigot.events.enums.ArenaJoinCause;
import fun.ogtimes.skywars.spigot.events.enums.ArenaLeaveCause;
import fun.ogtimes.skywars.spigot.listener.DamageListener;
import fun.ogtimes.skywars.spigot.player.SkyPlayer;
import fun.ogtimes.skywars.spigot.utils.Game;
import fun.ogtimes.skywars.spigot.utils.ProxyUtils;
import fun.ogtimes.skywars.spigot.utils.Messages;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CmdOthers implements CommandExecutor {

    public CmdOthers(SkyWars instance) {
        instance.getCommand("leave").setExecutor(this);
        instance.getCommand("salir").setExecutor(this);
        instance.getCommand("playagain").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;

        SkyPlayer skyPlayer = SkyWars.getSkyPlayer(player);
        if (skyPlayer == null) {
            return false;
        }

        switch (label.toLowerCase()) {
            case "leave":
            case "salir":
                skyPlayer.leave();
                break;
            case "playagain":
                skyPlayer.leave();
                try {
                    if (!skyPlayer.isInArena()) {
                        if (GameQueue.withoutGames()) {
                            GameQueue.addPlayer(skyPlayer);
                            skyPlayer.sendMessage("&cNo games available, you have been added to the queue");
                            return true;
                        }
                    }

                    Game game = GameQueue.getJoinableGame();
                    if (game == null) {
                        GameQueue.addPlayer(skyPlayer);
                        skyPlayer.sendMessage("&cNo games available, you have been added to the queue");
                        return true;
                    }

                    if (SkyWars.isMultiArenaMode()) {
                        Arena arena = (Arena)game;
                        arena.addPlayer(skyPlayer, ArenaJoinCause.COMMAND);
                    } else if (SkyWars.isLobbyMode()) {
                        ProxyUtils.teleToServer(skyPlayer.getPlayer(), "", game.getName());
                    }

                } catch (Exception e) {
                    skyPlayer.sendMessage("&cAn error occurred while trying to join a new game.");
                    return true;
                }
                break;
        }

        return true;
    }
}
