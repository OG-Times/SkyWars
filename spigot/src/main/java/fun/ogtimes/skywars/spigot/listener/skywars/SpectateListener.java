package fun.ogtimes.skywars.spigot.listener.skywars;

import fun.ogtimes.skywars.spigot.SkyWars;
import fun.ogtimes.skywars.spigot.arena.Arena;
import fun.ogtimes.skywars.spigot.config.ConfigManager;
import fun.ogtimes.skywars.spigot.events.SkyPlayerSpectatorEvent;
import fun.ogtimes.skywars.spigot.events.enums.ArenaLeaveCause;
import fun.ogtimes.skywars.spigot.player.SkyPlayer;
import fun.ogtimes.skywars.spigot.utils.ProxyUtils;
import fun.ogtimes.skywars.spigot.utils.Messages;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class SpectateListener implements Listener {
   @EventHandler
   public void onSkyPlayerSpectator(SkyPlayerSpectatorEvent var1) {
      SkyPlayer var2 = var1.getPlayer();
      Arena var3 = var1.getGame();
      if (!var1.isLeaveReason() && var1.getSpectate()) {
         if (var1.getSpectate() && var1.isDeathReason()) {
            if (ConfigManager.main.getBoolean("options.disableSpectatorMode-Death")) {
               if (SkyWars.isProxyMode()) {
                  var3.removePlayer(var2, ArenaLeaveCause.SPECTATOR_DISABLED_ON_DEATH);
                  ProxyUtils.teleToServer(var2.getPlayer(), SkyWars.getMessage(Messages.PLAYER_TELEPORT_LOBBY), SkyWars.getRandomLobby());
               } else {
                  var3.removePlayer(var2, ArenaLeaveCause.SPECTATOR_DISABLED_ON_DEATH);
               }

               return;
            }

            if (SkyWars.is18orHigher()) {
               if (var2.getPlayer().getGameMode() != GameMode.SPECTATOR) {
                  var2.getPlayer().setGameMode(GameMode.SPECTATOR);
               }
            } else if (SkyWars.isProxyMode()) {
               var3.removePlayer(var2, ArenaLeaveCause.SPECTATOR_IN_LOWER_VERSION);
               ProxyUtils.teleToServer(var2.getPlayer(), SkyWars.getMessage(Messages.PLAYER_TELEPORT_LOBBY), SkyWars.getRandomLobby());
            } else {
               var3.removePlayer(var2, ArenaLeaveCause.SPECTATOR_IN_LOWER_VERSION);
            }
         }

         if (var1.getSpectate() && var1.isJoinReason()) {
            if (SkyWars.is18orHigher()) {
               if (var2.getPlayer().getGameMode() != GameMode.SPECTATOR) {
                  var2.getPlayer().setGameMode(GameMode.SPECTATOR);
                  var2.sendMessage(SkyWars.getMessage(Messages.PLAYER_DEATH));
               }
            } else if (SkyWars.isProxyMode()) {
               var3.removePlayer(var2, ArenaLeaveCause.SPECTATOR_JOIN_IN_LOWER_VERSION);
               ProxyUtils.teleToServer(var2.getPlayer(), SkyWars.getMessage(Messages.PLAYER_TELEPORT_LOBBY), SkyWars.getRandomLobby());
            } else {
               var3.removePlayer(var2, ArenaLeaveCause.SPECTATOR_JOIN_IN_LOWER_VERSION);
            }
         }

      } else {
         if (var2.getPlayer().getGameMode() != GameMode.SURVIVAL) {
            var2.getPlayer().setGameMode(GameMode.SURVIVAL);
         }

      }
   }
}
