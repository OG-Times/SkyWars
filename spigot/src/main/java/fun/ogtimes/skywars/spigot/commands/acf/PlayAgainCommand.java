package fun.ogtimes.skywars.spigot.commands.acf;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import fun.ogtimes.skywars.spigot.SkyWars;
import fun.ogtimes.skywars.spigot.arena.Arena;
import fun.ogtimes.skywars.spigot.arena.GameQueue;
import fun.ogtimes.skywars.spigot.events.enums.ArenaJoinCause;
import fun.ogtimes.skywars.spigot.player.SkyPlayer;
import fun.ogtimes.skywars.spigot.utils.Game;
import fun.ogtimes.skywars.spigot.utils.ProxyUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * This code was made by jsexp, in case of any unauthorized
 * use, at least please leave credits.
 * Find more about me @ my <a href="https://github.com/hardcorefactions">GitHub</a> :D
 * © 2025 - jsexp
 */
@CommandAlias("playagain")
public class PlayAgainCommand extends BaseCommand {

    @Default
    public void playagain(Player player) {
        SkyPlayer skyPlayer = SkyWars.getSkyPlayer(player);
        if (skyPlayer == null) {
            return;
        }

        skyPlayer.leave();
        try {
            if (!skyPlayer.isInArena()) {
                if (GameQueue.withoutGames()) {
                    GameQueue.addPlayer(skyPlayer);
                    skyPlayer.sendMessage("&cNo games available, you have been added to the queue");
                    return;
                }
            }

            Game game = GameQueue.getJoinableGame();
            if (game == null) {
                GameQueue.addPlayer(skyPlayer);
                skyPlayer.sendMessage("&cNo games available, you have been added to the queue");
                return;
            }

            if (SkyWars.isMultiArenaMode()) {
                Arena arena = (Arena)game;
                arena.addPlayer(skyPlayer, ArenaJoinCause.COMMAND);
            } else if (SkyWars.isLobbyMode()) {
                ProxyUtils.teleToServer(skyPlayer.getPlayer(), "", game.getName());
            }

        } catch (Exception e) {
            skyPlayer.sendMessage("&cAn error occurred while trying to join a new game.");
        }
    }

}
