package fun.ogtimes.skywars.spigot.commands.admin.edit.lobby;

import fun.ogtimes.skywars.spigot.SkyWars;
import fun.ogtimes.skywars.spigot.commands.BaseCommand;
import fun.ogtimes.skywars.spigot.config.ConfigManager;
import fun.ogtimes.skywars.spigot.utils.LocationUtil;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CmdLobbySpawn implements BaseCommand {
    public void onCommand(CommandSender sender, String[] args) {
        Player player;
        if (!(sender instanceof Player)) {
            sender.sendMessage("You aren't a player!");
        } else {
            player = (Player)sender;
            if (!player.hasPermission(this.getPermission())) {
                player.sendMessage("§cYou do not have permission!");
            } else if (args.length == 0) {
                SkyWars.getPlugin().getConfig().set("spawn", LocationUtil.getString(player.getLocation(), true));
                ConfigManager.main.set("spawn", LocationUtil.getString(player.getLocation(), true));
                ConfigManager.main.save();
                SkyWars.spawn = player.getLocation();
                player.sendMessage("§aLobby Spawn set");
            } else {
            }
        }
    }

    public String help(CommandSender sender) {
        String var2 = "&a/sw lobbyspawn &a- &bSet lobby spawn";
        return sender.hasPermission(this.getPermission()) ? var2 : "";
    }

    public String getPermission() {
        return "skywars.admin";
    }

    public boolean console() {
        return false;
    }

    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return null;
    }
}
