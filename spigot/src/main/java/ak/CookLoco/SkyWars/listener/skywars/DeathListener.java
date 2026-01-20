package ak.CookLoco.SkyWars.listener.skywars;

import ak.CookLoco.SkyWars.SkyWars;
import ak.CookLoco.SkyWars.arena.Arena;
import ak.CookLoco.SkyWars.events.SkyPlayerDeathEvent;
import ak.CookLoco.SkyWars.listener.DamageListener;
import ak.CookLoco.SkyWars.player.SkyPlayer;
import ak.CookLoco.SkyWars.utils.Messages;
import ak.CookLoco.SkyWars.utils.economy.SkyEconomyManager;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class DeathListener implements Listener {
   @EventHandler
   public void onSkyPlayerDeath(SkyPlayerDeathEvent var1) {
      Bukkit.getScheduler().runTaskAsynchronously(SkyWars.getPlugin(), () -> {
         SkyPlayer var1x = var1.getPlayer();
         SkyPlayer var2 = var1.getKiller();
         Arena var3 = var1.getGame();
         DamageListener.lastDamage.remove(var1x.getUniqueId());
         var1x.addDeaths(1);
         var1x.playedTimeEnd();
         var1x.distanceWalkedConvert();
         if (var2 != null) {
            if (!var1x.getName().equals(var2.getName())) {
               var2.addKills(1);
               var3.broadcast(String.format(SkyWars.getMessage(Messages.GAME_PLAYER_DEATH_PLAYER), var1x.getName(), var2.getName()));
               SkyEconomyManager.addCoins(var2.getPlayer(), (double)SkyWars.getPlugin().getConfig().getInt("reward.kill"), true);
               var3.addKillStreak(var2);
            } else {
               var3.broadcast(String.format(SkyWars.getMessage(Messages.GAME_PLAYER__DEATH_OTHER), var1x.getName()));
            }
         } else {
            var3.broadcast(String.format(SkyWars.getMessage(Messages.GAME_PLAYER__DEATH_OTHER), var1x.getName()));
         }

         var1x.sendMessage(SkyWars.getMessage(Messages.PLAYER_DEATH));
         var3.broadcast(String.format(SkyWars.getMessage(Messages.GAME_PLAYERS_REMAIN), var3.getAlivePlayers()));
         Bukkit.getScheduler().runTask(SkyWars.getPlugin(), () -> {
            ArenaListener.checkWinner(var3);
         });
         var3.getAlivePlayer().forEach((var0) -> {
            SkyEconomyManager.addCoins(var0.getPlayer(), (double)SkyWars.getPlugin().getConfig().getInt("reward.death"), true);
         });
      });
   }
}
