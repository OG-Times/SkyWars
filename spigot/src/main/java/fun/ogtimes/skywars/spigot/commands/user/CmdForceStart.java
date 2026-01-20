package fun.ogtimes.skywars.spigot.commands.user;

import fun.ogtimes.skywars.spigot.SkyWars;
import fun.ogtimes.skywars.spigot.arena.Arena;
import fun.ogtimes.skywars.spigot.commands.BaseCommand;
import fun.ogtimes.skywars.spigot.player.SkyPlayer;
import fun.ogtimes.skywars.spigot.utils.Messages;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CmdForceStart implements BaseCommand {
   public void onCommand(CommandSender var1, String[] var2) {
      Player var3 = null;
      if (!(var1 instanceof Player)) {
         var1.sendMessage("You aren't a player!");
      } else {
         var3 = (Player)var1;
         if (!var3.hasPermission(this.getPermission())) {
            var3.sendMessage("§cYou do not have permission!");
         } else {
            if (var2.length < 1) {
               SkyPlayer var4 = SkyWars.getSkyPlayer(var3);
               if (var4.isInArena()) {
                  if (var4.getArena().getPlayers().size() <= 1) {
                     var4.sendMessage("&cYou need at least two (2) players to force the game");
                     return;
                  }

                  Arena var5 = var4.getArena();
                  var5.setForceStart();
                  var5.broadcast(SkyWars.getMessage(Messages.GAME_FORCESTART));
               }
            }

         }
      }
   }

   public String help(CommandSender var1) {
      String var2 = "&a/sw &eforcestart &a- &bForce to start game";
      return var1.hasPermission(this.getPermission()) ? var2 : "";
   }

   public String getPermission() {
      return "skywars.admin.forcestart";
   }

   public boolean console() {
      return false;
   }

   public List<String> onTabComplete(CommandSender var1, String[] var2) {
      return null;
   }
}
