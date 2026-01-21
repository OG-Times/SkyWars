package fun.ogtimes.skywars.spigot.commands.user;

import fun.ogtimes.skywars.spigot.SkyWars;
import fun.ogtimes.skywars.spigot.commands.BaseCommand;
import fun.ogtimes.skywars.spigot.menus.MenuListener;
import fun.ogtimes.skywars.spigot.player.SkyPlayer;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CmdOpen implements BaseCommand {
   public void onCommand(CommandSender sender, String[] args) {
      if (!(sender instanceof Player var3)) {
         sender.sendMessage("Only for Players");
      } else {
          if (args.length != 1) {
            var3.sendMessage("§cUsage: /sw open lshop");
         } else {
            String var4 = args[0];
            byte var5 = -1;
             if (var4.hashCode() == 103269730) {
                 if (var4.equals("lshop")) {
                     var5 = 0;
                 }
             }

             if (var5 == 0) {
                 if (var3.hasPermission("skywars.cmd.open.lshop")) {
                     SkyPlayer var6 = SkyWars.getSkyPlayer(var3);
                     if (var6 == null) {
                     } else if (SkyWars.isMultiArenaMode() && !var6.isInArena() || SkyWars.isLobbyMode()) {
                         var3.openInventory(MenuListener.getPlayerMenu(var6.getPlayer(), "shop").getInventory());
                     }
                 }
             }
         }
      }
   }

   public String help(CommandSender sender) {
      String var2 = "&a/sw &eopen lshop &a- &bOpen Lobby Kit Shop Inventory";
      return sender.hasPermission(this.getPermission()) ? var2 : "";
   }

   public String getPermission() {
      return "skywars.cmd.open";
   }

   public boolean console() {
      return false;
   }

   public List<String> onTabComplete(CommandSender sender, String[] args) {
      if (args.length >= 1) {
         ArrayList var3 = new ArrayList();
         var3.add("lshop");
         return var3;
      } else {
         return null;
      }
   }
}
