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
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player killed = event.getEntity();
        SkyPlayer skyPlayer = SkyWars.getSkyPlayer(killed);
        Player killer = event.getEntity().getKiller();
        SkyPlayer killerSkyPlayer;
        if (killer == null) {
            killerSkyPlayer = null;
        } else {
            killerSkyPlayer = SkyWars.getSkyPlayer(killer);
        }

        if (skyPlayer != null) {
            if (skyPlayer.isInArena()) {
                Arena arena = skyPlayer.getArena();
                event.setDeathMessage(null);
                skyPlayer.setSpectating(true, SpectatorReason.DEATH);
                killed.setHealth(killed.getMaxHealth());
                Bukkit.getScheduler().runTaskLater(SkyWars.getPlugin(), () -> killed.spigot().respawn(), 1L);

                SkyPlayerDeathEvent skyPlayerDeathEvent = new SkyPlayerDeathEvent(skyPlayer, killerSkyPlayer, arena, event);
                Bukkit.getServer().getPluginManager().callEvent(skyPlayerDeathEvent);
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
