package fun.ogtimes.skywars.spigot.commands.admin.edit.lobby;

import fun.ogtimes.skywars.spigot.SkyWars;
import fun.ogtimes.skywars.spigot.commands.BaseCommand;
import fun.ogtimes.skywars.spigot.config.ConfigManager;
import fun.ogtimes.skywars.spigot.utils.LocationUtil;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CmdLobbySpawn implements BaseCommand {
   public boolean onCommand(CommandSender var1, String[] var2) {
      Player var3 = null;
      if (!(var1 instanceof Player)) {
         var1.sendMessage("You aren't a player!");
         return true;
      } else {
         var3 = (Player)var1;
         if (!var3.hasPermission(this.getPermission())) {
            var3.sendMessage("§cYou do not have permission!");
            return true;
         } else if (var2.length == 0) {
            SkyWars.getPlugin().getConfig().set("spawn", LocationUtil.getString(var3.getLocation(), true));
            ConfigManager.main.set("spawn", LocationUtil.getString(var3.getLocation(), true));
            ConfigManager.main.save();
            SkyWars.spawn = var3.getLocation();
            var3.sendMessage("§aLobby Spawn set");
            return true;
         } else {
            return true;
         }
      }
   }

   public String help(CommandSender var1) {
      String var2 = "&a/sw lobbyspawn &a- &bSet lobby spawn";
      return var1.hasPermission(this.getPermission()) ? var2 : "";
   }

   public String getPermission() {
      return "skywars.admin";
   }

   public boolean console() {
      return false;
   }

   public List<String> onTabComplete(CommandSender var1, String[] var2) {
      return null;
   }
}
