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
   public boolean onCommand(CommandSender var1, String[] var2) {
      if (!(var1 instanceof Player)) {
         var1.sendMessage("You aren't a player!");
         return true;
      } else {
         Player var3 = (Player)var1;
         SkyPlayer var4 = SkyWars.getSkyPlayer(var3);
         if (var4 == null) {
            return false;
         } else if (!var3.hasPermission(this.getPermission())) {
            var3.sendMessage("§cYou do not have permission!");
            return true;
         } else {
            Arena var6;
            if (var2.length == 0) {
               if (!var4.isInArena()) {
                  if (GameQueue.withoutGames()) {
                     GameQueue.addPlayer(var4);
                     var4.sendMessage("&cNo games available, you has been added to the queue");
                     return true;
                  }

                  Game var5 = GameQueue.getJoinableGame();
                  if (var5 == null) {
                     GameQueue.addPlayer(var4);
                     var4.sendMessage("&cNo games available, you has been added to the queue");
                     return true;
                  }

                  if (SkyWars.isMultiArenaMode()) {
                     var6 = (Arena)var5;
                     var6.addPlayer(var4, ArenaJoinCause.COMMAND);
                  } else if (SkyWars.isLobbyMode()) {
                     ProxyUtils.teleToServer(var4.getPlayer(), "", var5.getName());
                  }

                  return true;
               }
            } else if (var2.length == 1 && SkyWars.isMultiArenaMode() && !var4.isInArena()) {
               String var7 = var2[0];
               var6 = ArenaManager.getGame(var7);
               if (var6 == null) {
                  var4.sendMessage("&cThis arena doesn't exists");
                  return false;
               }

               if (var6.getState() == ArenaState.INGAME && !var3.hasPermission("skywars.admin.spectate")) {
                  var4.sendMessage(SkyWars.getMessage(Messages.GAME_INGAME_MESSAGE));
                  return false;
               }

               if (var6.getAlivePlayers() >= var6.getMaxPlayers() && !var3.hasPermission("skywars.admin.spectate")) {
                  var4.sendMessage(SkyWars.getMessage(Messages.GAME_FULL_MESSAGE));
                  return false;
               }

               if (var6.isLoading()) {
                  var4.sendMessage(SkyWars.getMessage(Messages.GAME_LOADING));
                  return false;
               }

               var6.addPlayer(var4, ArenaJoinCause.COMMAND);
               return true;
            }

            return true;
         }
      }
   }

   public String help(CommandSender var1) {
      String var2 = "&a/sw join &e[ArenaName] &a- &bJoin to a random or specific";
      if (SkyWars.isLobbyMode()) {
         var2 = "&a/sw join &a- &bJoin to a random game";
      }

      return var1.hasPermission(this.getPermission()) ? var2 : "";
   }

   public String getPermission() {
      return "skywars.join";
   }

   public boolean console() {
      return false;
   }

   public List<String> onTabComplete(CommandSender var1, String[] var2) {
      if (var1.hasPermission(this.getPermission()) && !SkyWars.isLobbyMode()) {
         if (var2.length != 1) {
            return null;
         } else {
            ArrayList var3 = new ArrayList();
            ArrayList var4 = new ArrayList();
            Iterator var5 = ArenaManager.getGames().iterator();

            while(true) {
               Arena var6;
               do {
                  if (!var5.hasNext()) {
                     var1.sendMessage("--------------------------------------------");
                     var1.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aAvailable games (&b" + var3.size() + "&a):"));
                     StringUtil.copyPartialMatches(var2[0], var3, var4);
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
