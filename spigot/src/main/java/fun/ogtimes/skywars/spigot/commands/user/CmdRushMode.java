package fun.ogtimes.skywars.spigot.commands.user;

import fun.ogtimes.skywars.spigot.SkyWars;
import fun.ogtimes.skywars.spigot.arena.Arena;
import fun.ogtimes.skywars.spigot.arena.GameQueue;
import fun.ogtimes.skywars.spigot.commands.BaseCommand;
import fun.ogtimes.skywars.spigot.events.enums.ArenaJoinCause;
import fun.ogtimes.skywars.spigot.player.SkyPlayer;
import fun.ogtimes.skywars.spigot.utils.Game;
import fun.ogtimes.skywars.spigot.utils.Messages;
import fun.ogtimes.skywars.spigot.utils.ProxyUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class CmdRushMode implements BaseCommand {

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) return;

        SkyPlayer skyPlayer = SkyWars.getSkyPlayer(player);
        if (skyPlayer == null) return;

        if (skyPlayer.isSpectating()) {
            skyPlayer.doRushMode();
        }

        boolean newState = !skyPlayer.isRushMode();
        skyPlayer.setRushMode(newState);

        skyPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&',
                newState
                ? SkyWars.getMessage(Messages.RUSH_MODE_ENABLED)
                : SkyWars.getMessage(Messages.RUSH_MODE_DISABLED))
        );
    }

    @Override
    public String help(CommandSender sender) {
        return "&a/sw rush &7- Toggle Rush Mode (auto play again)";
    }

    @Override
    public String getPermission() {
        return "skywars.user";
    }

    @Override
    public boolean console() {
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}
