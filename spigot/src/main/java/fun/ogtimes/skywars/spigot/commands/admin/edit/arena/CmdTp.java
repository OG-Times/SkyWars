package fun.ogtimes.skywars.spigot.commands.admin.edit.arena;

import fun.ogtimes.skywars.spigot.commands.BaseCommand;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

public class CmdTp implements BaseCommand {
   public void onCommand(CommandSender var1, String[] var2) {
      Player var3 = null;
      if (!(var1 instanceof Player)) {
         var1.sendMessage("You aren't a player!");
      } else {
         var3 = (Player)var1;
         if (!var3.hasPermission(this.getPermission())) {
            var3.sendMessage("§cYou do not have permission!");
         } else if (var2.length == 1) {
            World var7 = Bukkit.getWorld(var2[0]);
            if (var7 == null) {
               var3.sendMessage("§cThat world does not exist!");
            } else {
               var3.getPlayer().teleport(var7.getSpawnLocation());
            }
         } else {
            var3.sendMessage("§cUsage: /sw tp <world>");
            StringBuilder var4 = new StringBuilder();

             for (World var6 : Bukkit.getWorlds()) {
                 var4.append(", ").append(var6.getName());
             }

            var3.sendMessage(String.format("§cWorlds Loaded List: %s", var4.toString().replaceFirst(", ", "")));
         }
      }
   }

   public String help(CommandSender var1) {
      String var2 = "&a/sw tp &e<world> &a- &bTeleport to another world";
      return var1.hasPermission(this.getPermission()) ? var2 : "";
   }

   public String getPermission() {
      return "skywars.admin.tp";
   }

   public boolean console() {
      return false;
   }

   public List<String> onTabComplete(CommandSender var1, String[] var2) {
      if (!var1.hasPermission(this.getPermission())) {
         return null;
      } else if (var2.length != 1) {
         return null;
      } else {
         ArrayList var3 = new ArrayList();
         ArrayList var4 = new ArrayList();

          for (World var6 : Bukkit.getWorlds()) {
              var3.add(var6.getName());
          }

         var1.sendMessage("--------------------------------------------");
         var1.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aWorlds loaded (&b" + var3.size() + "&a):"));
         StringUtil.copyPartialMatches(var2[0], var3, var4);
         Collections.sort(var4);
         return var4;
      }
   }
}
