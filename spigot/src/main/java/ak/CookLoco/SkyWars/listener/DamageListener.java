package ak.CookLoco.SkyWars.listener;

import ak.CookLoco.SkyWars.SkyWars;
import ak.CookLoco.SkyWars.arena.Arena;
import ak.CookLoco.SkyWars.arena.ArenaState;
import ak.CookLoco.SkyWars.config.ConfigManager;
import ak.CookLoco.SkyWars.player.SkyPlayer;
import java.util.HashMap;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class DamageListener implements Listener {
   public static HashMap<UUID, UUID> lastDamage = new HashMap();

   @EventHandler
   public void onPlayerDamage(EntityDamageEvent var1) {
      if (var1.getEntity() instanceof Player) {
         Player var2 = (Player)var1.getEntity();
         SkyPlayer var3 = SkyWars.getSkyPlayer(var2);
         if (var3 == null) {
            SkyWars.log("DamageListener.onPlayerDamage - null Player");
            return;
         }

         if (var3.isInArena()) {
            Arena var4 = var3.getArena();
            if (var4.getState() == ArenaState.WAITING || var4.getState() == ArenaState.STARTING || var4.getState() == ArenaState.ENDING) {
               var1.setCancelled(true);
            }

            if (var1.getCause() == DamageCause.FALL && !var4.isFallDamage() && var4.getState() == ArenaState.INGAME) {
               var1.setCancelled(true);
            }

            if (var3.isSpectating()) {
               var3.getPlayer().setFireTicks(0);
               var1.setCancelled(true);
            }
         } else if (SkyWars.getPlugin().getConfig().getBoolean("options.disableDamage-Outside-The-Arena")) {
            var1.setCancelled(true);
         }
      }

   }

   @EventHandler
   public void onPlayerDamageByPlayer(EntityDamageByEntityEvent var1) {
      if (var1.getEntity() instanceof Player && var1.getDamager() instanceof Player) {
         Player var2 = (Player)var1.getEntity();
         Player var3 = (Player)var1.getDamager();
         SkyPlayer var4 = SkyWars.getSkyPlayer(var2);
         SkyPlayer var5 = SkyWars.getSkyPlayer(var3);
         if (var4 == null) {
            SkyWars.log("DamageListener.onPlayerDamageByPlayer - null Player");
            return;
         }

         if (var5 == null) {
            SkyWars.log("DamageListener.onPlayerDamageByPlayer - null Damage Player");
            return;
         }

         if (var4.isInArena()) {
            Arena var6 = var4.getArena();
            if (var5.isSpectating()) {
               var1.setCancelled(true);
            }

            if (var6.getState() == ArenaState.WAITING || var6.getState() == ArenaState.STARTING || var6.getState() == ArenaState.ENDING) {
               var1.setCancelled(true);
            }

            if (var6.getState() == ArenaState.INGAME && !var4.isSpectating() && !var5.isSpectating()) {
               lastDamage.put(var2.getUniqueId(), var3.getUniqueId());
               Bukkit.getScheduler().runTaskLater(SkyWars.getPlugin(), () -> {
                  lastDamage.remove(var2.getUniqueId(), var3.getUniqueId());
               }, 20L * (long)ConfigManager.main.getInt("options.combatLogTime"));
            }
         } else if (SkyWars.getPlugin().getConfig().getBoolean("options.disablePvP-Outside-The-Arena")) {
            var1.setCancelled(true);
         }
      }

   }
}
