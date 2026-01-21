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
        if (!SkyWars.isMultiArenaMode()) return;

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

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player;
        if (SkyWars.isMultiArenaMode()) {
            player = event.getPlayer();
            World world = player.getWorld();

            for (Player online : Bukkit.getServer().getOnlinePlayers()) {
                if (online != player) {
                    if (online.getWorld() == world) {
                        online.showPlayer(player);
                        player.showPlayer(online);
                    } else if (!ConfigManager.main.getBoolean("options.perWorldTabBypass")) {
                        online.hidePlayer(player);
                        player.hidePlayer(online);
                    } else {
                        if (!online.hasPermission("skywars.tab.bypass")) {
                            online.hidePlayer(player);
                        }

                        if (!player.hasPermission("skywars.tab.bypass")) {
                            player.hidePlayer(online);
                        }
                    }
                }
            }
        } else if (SkyWars.isProxyMode()) {
            player = event.getPlayer();
            SkyPlayer skyPlayer = SkyWars.getSkyPlayer(player);
            if (skyPlayer != null && skyPlayer.isInArena()) {
                Arena arena = skyPlayer.getArena();

                for (SkyPlayer alivePlayer : arena.getAlivePlayer()) {
                    if (alivePlayer != skyPlayer) {
                        alivePlayer.getPlayer().hidePlayer(player);
                        player.hidePlayer(alivePlayer.getPlayer());
                    }
                }

                Bukkit.getScheduler().runTaskLater(SkyWars.getPlugin(), () -> {

                    for (SkyPlayer alivePlayer : arena.getAlivePlayer()) {
                        if (alivePlayer != skyPlayer) {
                            alivePlayer.getPlayer().showPlayer(player);
                            player.showPlayer(alivePlayer.getPlayer());
                        }
                    }

                }, 20L);
            }
        }

    }
}
