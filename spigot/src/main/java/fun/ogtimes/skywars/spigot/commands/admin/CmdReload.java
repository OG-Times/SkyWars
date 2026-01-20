package fun.ogtimes.skywars.spigot.commands.admin;

import fun.ogtimes.skywars.spigot.SkyWars;
import fun.ogtimes.skywars.spigot.commands.BaseCommand;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

public class CmdReload implements BaseCommand {
   public void onCommand(CommandSender var1, String[] var2) {
      Player var3 = null;
      if (!(var1 instanceof Player)) {
         var1.sendMessage("You aren't a player!");
      } else {
         var3 = (Player)var1;
         if (!var3.hasPermission(this.getPermission())) {
            var3.sendMessage("§cYou do not have permission!");
         } else if (var2.length == 0) {
            this.helpDefault(var1);
         } else {
            if (var2.length >= 1) {
               String var4 = var2[0].toLowerCase();
               byte var5 = -1;
               switch(var4.hashCode()) {
               case -1354792126:
                  if (var4.equals("config")) {
                     var5 = 0;
                  }
                  break;
               case -462094004:
                  if (var4.equals("messages")) {
                     var5 = 1;
                  }
                  break;
               case 96673:
                  if (var4.equals("all")) {
                     var5 = 4;
                  }
                  break;
               case 109264530:
                  if (var4.equals("score")) {
                     var5 = 2;
                  }
                  break;
               case 109413437:
                  if (var4.equals("shops")) {
                     var5 = 3;
                  }
               }

               switch(var5) {
               case 0:
                  if (!var1.hasPermission("skywars.admin.reload.config")) {
                     return;
                  }

                  this.reloadConfig(var1, true);
                  break;
               case 1:
                  if (!var1.hasPermission("skywars.admin.reload.messages")) {
                     return;
                  }

                  this.reloadMessages(var1, true);
                  break;
               case 2:
                  if (!var1.hasPermission("skywars.admin.reload.score")) {
                     return;
                  }

                  this.reloadScoreboard(var1, true);
                  break;
               case 3:
                  if (!var1.hasPermission("skywars.admin.reload.shops")) {
                     return;
                  }

                  this.reloadShops(var1, true);
                  break;
               case 4:
                  if (!var1.hasPermission("skywars.admin.reload.all")) {
                     return;
                  }

                  var1.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a" + SkyWars.prefix + "&eReloading all files"));
                  this.reloadConfig(var1, false);
                  this.reloadMessages(var1, false);
                  this.reloadScoreboard(var1, false);
                  this.reloadShops(var1, false);
                  var1.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a" + SkyWars.prefix + "All files reloaded"));
                  break;
               default:
                  this.helpDefault(var1);
               }
            }

         }
      }
   }

   public String help(CommandSender var1) {
      String var2 = "&a/sw reload - &bFor more help about reload command";
      return var1.hasPermission(this.getPermission()) ? var2 : "";
   }

   public String getPermission() {
      return "skywars.admin.reload";
   }

   public boolean console() {
      return false;
   }

   private void reloadConfig(CommandSender var1, boolean var2) {
      if (var2) {
         var1.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a" + SkyWars.prefix + "&eReloading config file"));
      }

      try {
         SkyWars.reloadConfigMain();
         if (var2) {
            var1.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a" + SkyWars.prefix + "Config file reloaded"));
         }
      } catch (Exception var4) {
         var1.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c" + SkyWars.prefix + "An error occurred while try to reload config file, please check console log"));
         SkyWars.getPlugin().getLogger().log(Level.SEVERE, "An error occurred in config.yml", var4);
      }

   }

   private void reloadMessages(CommandSender var1, boolean var2) {
      if (var2) {
         var1.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a" + SkyWars.prefix + "&eReloading messages files"));
      }

      try {
         SkyWars.reloadMessages();
         if (var2) {
            var1.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a" + SkyWars.prefix + "Messages files reloaded"));
         }
      } catch (Exception var4) {
         var1.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c" + SkyWars.prefix + "An error occurred while try to reload messages files, please check console log"));
         SkyWars.getPlugin().getLogger().log(Level.SEVERE, "An error occurred in some message file", var4);
      }

   }

   private void reloadScoreboard(CommandSender var1, boolean var2) {
      if (var2) {
         var1.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a" + SkyWars.prefix + "&eReloading scoreboard file"));
      }

      try {
         SkyWars.reloadConfigScoreboard();
         if (var2) {
            var1.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a" + SkyWars.prefix + "Scoreboard file reloaded"));
         }
      } catch (Exception var4) {
         var1.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c" + SkyWars.prefix + "An error occurred while try to reload scoreboard file, please check console log"));
         SkyWars.getPlugin().getLogger().log(Level.SEVERE, "An error occurred in scoreboard.yml", var4);
      }

   }

   private void reloadShops(CommandSender var1, boolean var2) {
      if (var2) {
         var1.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a" + SkyWars.prefix + "&eReloading shop file"));
      }

      try {
         SkyWars.reloadConfigShop();
         if (var2) {
            var1.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a" + SkyWars.prefix + "Shop file reloaded"));
         }
      } catch (Exception var6) {
         var1.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c" + SkyWars.prefix + "An error occurred while try to reload shop file, please check console log"));
         SkyWars.getPlugin().getLogger().log(Level.SEVERE, "An error occurred in shop.yml", var6);
      }

      var1.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a" + SkyWars.prefix + "&eReloading abilities file"));

      try {
         SkyWars.reloadConfigAbilities();
         var1.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a" + SkyWars.prefix + "Abilities file reloaded"));
      } catch (Exception var5) {
         var1.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c" + SkyWars.prefix + "An error occurred while try to reload abilities file, please check console log"));
         SkyWars.getPlugin().getLogger().log(Level.SEVERE, "An error occurred in abilities.yml", var5);
      }

      try {
         SkyWars.reloadAbilities();
      } catch (Exception var4) {
         SkyWars.getPlugin().getLogger().log(Level.SEVERE, "An error occurred while try to reload abilities objects", var4);
      }

   }

   public void helpDefault(CommandSender var1) {
      HashMap var2 = new HashMap();
      var2.put("all", "&a/sw reload &call &a- &bExecute all previous sub commands");
      var2.put("config", "&a/sw reload &econfig &a- &bReload config.yml file");
      var2.put("messages", "&a/sw reload &emessages &a- &bReload all messages files");
      var2.put("score", "&a/sw reload &escore &a- &bReload scoreboard file");
      var2.put("shops", "&a/sw reload &eshops &a- &bReload shop menu and messages");
      var1.sendMessage("------------ &a[SkyWars Reload Help] &f------------");

       for (Object object : var2.entrySet()) {
           Entry var4 = (Entry) object;
           if (var1.hasPermission("skywars.admin.reload." + var4.getKey())) {
               var1.sendMessage(ChatColor.translateAlternateColorCodes('&', (String) var4.getValue()));
           }
       }

      var1.sendMessage("--------------------------------------------");
   }

   public List<String> onTabComplete(CommandSender var1, String[] var2) {
      if (!var1.hasPermission(this.getPermission())) {
         return null;
      } else if (var2.length == 1) {
         HashMap var3 = new HashMap();
         var3.put("all", "&a/sw reload &call &a- &bExecute all previous sub commands");
         var3.put("config", "&a/sw reload &econfig &a- &bReload config.yml file");
         var3.put("messages", "&a/sw reload &emessages &a- &bReload all messages files");
         var3.put("score", "&a/sw reload &escore &a- &bReload scoreboard file");
         var3.put("shops", "&a/sw reload &eshops &a- &bReload shop menu and messages");
         String[] var4 = new String[]{"config", "messages", "score", "shops", "all"};
         ArrayList var5 = new ArrayList(Arrays.asList(var4));
         ArrayList var6 = new ArrayList();
         StringUtil.copyPartialMatches(var2[0], var5, var6);
         Collections.sort(var6);
         var1.sendMessage("--------------------------------------------");

          for (Object object : var3.entrySet()) {
              Entry var8 = (Entry) object;
              if (var6.contains(var8.getKey())) {
                  var1.sendMessage(ChatColor.translateAlternateColorCodes('&', (String) var8.getValue()));
              }
          }

         var1.sendMessage("--------------------------------------------");
         return var6;
      } else {
         return null;
      }
   }
}
