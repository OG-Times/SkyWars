package fun.ogtimes.skywars.spigot.listener;

import fun.ogtimes.skywars.spigot.SkyWars;
import fun.ogtimes.skywars.spigot.arena.Arena;
import fun.ogtimes.skywars.spigot.arena.ArenaState;
import fun.ogtimes.skywars.spigot.config.ConfigManager;
import fun.ogtimes.skywars.spigot.player.SkyPlayer;
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
    public static final HashMap<UUID, UUID> lastDamage = new HashMap<>();

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            SkyPlayer skyPlayer = SkyWars.getSkyPlayer(player);
            if (skyPlayer == null) {
                SkyWars.log("DamageListener.onPlayerDamage - null Player");
                return;
            }

            if (skyPlayer.isInArena()) {
                Arena arena = skyPlayer.getArena();
                if (arena.getState() == ArenaState.WAITING || arena.getState() == ArenaState.STARTING || arena.getState() == ArenaState.ENDING) {
                    event.setCancelled(true);
                }

                if (event.getCause() == DamageCause.FALL && !arena.isFallDamage() && arena.getState() == ArenaState.INGAME) {
                    event.setCancelled(true);
                }

                if (skyPlayer.isSpectating()) {
                    skyPlayer.getPlayer().setFireTicks(0);
                    event.setCancelled(true);
                }
            } else if (SkyWars.getPlugin().getConfig().getBoolean("options.disableDamage-Outside-The-Arena")) {
                event.setCancelled(true);
            }
        }

    }

    @EventHandler
    public void onPlayerDamageByPlayer(EntityDamageByEntityEvent var1) {
        if (var1.getEntity() instanceof Player var2 && var1.getDamager() instanceof Player var3) {
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
                    Bukkit.getScheduler().runTaskLater(SkyWars.getPlugin(), () -> lastDamage.remove(var2.getUniqueId(), var3.getUniqueId()), 20L * (long)ConfigManager.main.getInt("options.combatLogTime"));
                }
            } else if (SkyWars.getPlugin().getConfig().getBoolean("options.disablePvP-Outside-The-Arena")) {
                var1.setCancelled(true);
            }
        }

    }
}
