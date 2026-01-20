package fun.ogtimes.skywars.spigot.events;

import fun.ogtimes.skywars.spigot.SkyWars;
import fun.ogtimes.skywars.spigot.arena.Arena;
import fun.ogtimes.skywars.spigot.events.enums.SpectatorReason;
import fun.ogtimes.skywars.spigot.player.SkyPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class EventsManager implements Listener {
   @EventHandler
   public void onPlayerDeath(PlayerDeathEvent var1) {
      Player var2 = var1.getEntity();
      SkyPlayer var3 = SkyWars.getSkyPlayer(var2);
      Player var4 = var1.getEntity().getKiller();
      SkyPlayer var5;
      if (var4 == null) {
         var5 = null;
      } else {
         var5 = SkyWars.getSkyPlayer(var4);
      }

      if (var3 != null) {
         if (var3.isInArena()) {
            Arena var6 = var3.getArena();
            var1.setDeathMessage((String)null);
            var3.setSpectating(true, SpectatorReason.DEATH);
            if (SkyWars.is18orHigher()) {
               var2.setHealth(var2.getMaxHealth());
               Bukkit.getScheduler().runTaskLater(SkyWars.getPlugin(), () -> {
                  var2.spigot().respawn();
               }, 1L);
            }

            SkyPlayerDeathEvent var7 = new SkyPlayerDeathEvent(var3, var5, var6, var1);
            Bukkit.getServer().getPluginManager().callEvent(var7);
         }

      }
   }

   @EventHandler
   public void onPlayerRespawn(PlayerRespawnEvent var1) {
      Player var2 = var1.getPlayer();
      SkyPlayer var3 = SkyWars.getSkyPlayer(var2);
      if (var3 != null) {
         if (var3.isInArena()) {
            Arena var4 = var3.getArena();
            var1.setRespawnLocation(var4.getSpawn());
         } else {
            var1.setRespawnLocation(SkyWars.getSpawn());
         }

      }
   }
}
