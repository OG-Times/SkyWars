package fun.ogtimes.skywars.spigot.listener;

import fun.ogtimes.skywars.spigot.SkyWars;
import fun.ogtimes.skywars.spigot.arena.Arena;
import fun.ogtimes.skywars.spigot.config.ConfigManager;
import fun.ogtimes.skywars.spigot.player.SkyPlayer;
import java.util.Iterator;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class WorldTabListener implements Listener {
    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        if (SkyWars.is18orHigher() && SkyWars.isMultiArenaMode()) {
            Player eventPlayer = event.getPlayer();
            World worldTo = eventPlayer.getWorld();
            World worldFrom = event.getFrom();
            SkyPlayer skyPlayer = SkyWars.getSkyPlayer(eventPlayer);

            Bukkit.getScheduler().runTaskLater(SkyWars.getPlugin(), () -> {
                if (skyPlayer != null && skyPlayer.isInArena()) {
                    Arena arena = skyPlayer.getArena();

                    for (SkyPlayer alivePlayer : arena.getAlivePlayer()) {
                        if (alivePlayer != skyPlayer) {
                            alivePlayer.getPlayer().hidePlayer(eventPlayer);
                            eventPlayer.hidePlayer(alivePlayer.getPlayer());
                        }
                    }

                    Bukkit.getScheduler().runTaskLater(SkyWars.getPlugin(), () -> {

                        for (SkyPlayer skyAlivePlayer : arena.getAlivePlayer()) {
                            if (skyAlivePlayer != skyPlayer) {
                                skyAlivePlayer.getPlayer().showPlayer(eventPlayer);
                                eventPlayer.showPlayer(skyAlivePlayer.getPlayer());
                            }
                        }

                    }, 20L);
                } else {

                    for (Player player : worldTo.getPlayers()) {
                        if (player != eventPlayer) {
                            player.showPlayer(eventPlayer);
                            eventPlayer.showPlayer(player);
                        }
                    }
                }

            }, 20L);

            for (Player player : worldFrom.getPlayers()) {
                if (player != eventPlayer) {
                    if (!ConfigManager.main.getBoolean("options.perWorldTabBypass")) {
                        player.hidePlayer(eventPlayer);
                        eventPlayer.hidePlayer(player);
                    } else {
                        if (!player.hasPermission("skywars.tab.bypass")) {
                            player.hidePlayer(eventPlayer);
                        }

                        if (!eventPlayer.hasPermission("skywars.tab.bypass")) {
                            eventPlayer.hidePlayer(player);
                        }
                    }
                }
            }
        }

    }

    @EventHandler
    public void onJoin(PlayerJoinEvent var1) {
        Player var2;
        if (SkyWars.is18orHigher() && SkyWars.isMultiArenaMode()) {
            var2 = var1.getPlayer();
            World var7 = var2.getWorld();
            Iterator var8 = Bukkit.getServer().getOnlinePlayers().iterator();

            while(var8.hasNext()) {
                Player var9 = (Player)var8.next();
                if (var9 != var2) {
                    if (var9.getWorld() == var7) {
                        var9.showPlayer(var2);
                        var2.showPlayer(var9);
                    } else if (!ConfigManager.main.getBoolean("options.perWorldTabBypass")) {
                        var9.hidePlayer(var2);
                        var2.hidePlayer(var9);
                    } else {
                        if (!var9.hasPermission("skywars.tab.bypass")) {
                            var9.hidePlayer(var2);
                        }

                        if (!var2.hasPermission("skywars.tab.bypass")) {
                            var2.hidePlayer(var9);
                        }
                    }
                }
            }
        } else if (SkyWars.is18orHigher() && SkyWars.isProxyMode()) {
            var2 = var1.getPlayer();
            SkyPlayer var3 = SkyWars.getSkyPlayer(var2);
            if (var3 != null && var3.isInArena()) {
                Arena var4 = var3.getArena();
                Iterator var5 = var4.getAlivePlayer().iterator();

                while(var5.hasNext()) {
                    SkyPlayer var6 = (SkyPlayer)var5.next();
                    if (var6 != var3) {
                        var6.getPlayer().hidePlayer(var2);
                        var2.hidePlayer(var6.getPlayer());
                    }
                }

                Bukkit.getScheduler().runTaskLater(SkyWars.getPlugin(), () -> {
                    Iterator var3x = var4.getAlivePlayer().iterator();

                    while(var3x.hasNext()) {
                        SkyPlayer var4x = (SkyPlayer)var3x.next();
                        if (var4x != var3) {
                            var4x.getPlayer().showPlayer(var2);
                            var2.showPlayer(var4x.getPlayer());
                        }
                    }

                }, 20L);
            }
        }

    }
}
