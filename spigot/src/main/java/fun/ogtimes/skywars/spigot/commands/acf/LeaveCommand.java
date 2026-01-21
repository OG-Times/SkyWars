package fun.ogtimes.skywars.spigot.commands.acf;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import fun.ogtimes.skywars.spigot.SkyWars;
import fun.ogtimes.skywars.spigot.player.SkyPlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * This code was made by jsexp, in case of any unauthorized
 * use, at least please leave credits.
 * Find more about me @ my <a href="https://github.com/hardcorefactions">GitHub</a> :D
 * © 2025 - jsexp
 */
@CommandAlias("leave|salir")
public class LeaveCommand extends BaseCommand {

    @Default
    public void leave(Player player) {
        SkyPlayer skyPlayer = SkyWars.getSkyPlayer(player);
        if (skyPlayer == null) {
            return;
        }

        skyPlayer.leave();
    }

}
