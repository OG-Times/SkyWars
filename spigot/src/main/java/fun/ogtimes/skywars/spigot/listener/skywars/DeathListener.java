package fun.ogtimes.skywars.spigot.listener.skywars;

import fun.ogtimes.skywars.spigot.SkyWars;
import fun.ogtimes.skywars.spigot.arena.Arena;
import fun.ogtimes.skywars.spigot.arena.GameQueue;
import fun.ogtimes.skywars.spigot.events.SkyPlayerDeathEvent;
import fun.ogtimes.skywars.spigot.events.enums.ArenaJoinCause;
import fun.ogtimes.skywars.spigot.listener.DamageListener;
import fun.ogtimes.skywars.spigot.player.SkyPlayer;
import fun.ogtimes.skywars.spigot.utils.Game;
import fun.ogtimes.skywars.spigot.utils.Messages;
import fun.ogtimes.skywars.spigot.utils.ProxyUtils;
import fun.ogtimes.skywars.spigot.utils.Utils;
import fun.ogtimes.skywars.spigot.utils.economy.SkyEconomyManager;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

public class DeathListener implements Listener {

    @EventHandler
    public void onSkyPlayerDeath(SkyPlayerDeathEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(SkyWars.getPlugin(), () -> {
            SkyPlayer killed = event.getPlayer();
            SkyPlayer killer = event.getKiller();
            Arena arena = event.getGame();

            Audience killedAudience = SkyWars.getPlugin().getAdventure().player(killed.getPlayer());
            Component secondLine = Utils.component(SkyWars.getMessage(Messages.PLAY_AGAIN_2))
                    .replaceText(TextReplacementConfig.builder()
                            .match("<again>")
                            .replacement(Utils.component(SkyWars.getMessage(Messages.AGAIN))
                                    .clickEvent(ClickEvent.runCommand("/playagain"))
                            )
                            .build()
                    )
                    .replaceText(TextReplacementConfig.builder()
                            .match("<leave>")
                            .replacement(Utils.component(SkyWars.getMessage(Messages.LEAVE))
                                    .clickEvent(ClickEvent.runCommand("/salir"))
                            )
                            .build()
                    )
                    .replaceText(TextReplacementConfig.builder()
                            .match("<rush>")
                            .replacement(Utils.component(SkyWars.getMessage(Messages.RUSH_MODE))
                                    .clickEvent(ClickEvent.runCommand("/sw rush"))
                            )
                            .build()
                    );

            killed.sendMessage("        &m----------------------------------");
            killed.sendMessage(SkyWars.getMessage(Messages.PLAY_AGAIN_1));
            killedAudience.sendMessage(secondLine);
            killed.sendMessage("        &m----------------------------------");

            DamageListener.lastDamage.remove(killed.getUniqueId());
            killed.addDeaths(1);
            killed.playedTimeEnd();
            killed.distanceWalkedConvert();

            if (killer != null && !killed.getName().equals(killer.getName())) {
                killer.addKills(1);
                arena.broadcast(String.format(SkyWars.getMessage(Messages.GAME_PLAYER_DEATH_PLAYER), killed.getName(), killer.getName()));
                SkyEconomyManager.addCoins(killer.getPlayer(), SkyWars.getPlugin().getConfig().getInt("reward.kill"), true);
                arena.addKillStreak(killer);
            } else {
                arena.broadcast(String.format(SkyWars.getMessage(Messages.GAME_PLAYER__DEATH_OTHER), killed.getName()));
            }

            killed.sendMessage(SkyWars.getMessage(Messages.PLAYER_DEATH));
            arena.broadcast(String.format(SkyWars.getMessage(Messages.GAME_PLAYERS_REMAIN), arena.getAlivePlayers()));
            Bukkit.getScheduler().runTask(SkyWars.getPlugin(), () -> {
                ArenaListener.checkWinner(arena);
                arena.getAlivePlayer().forEach(alive -> SkyEconomyManager.addCoins(alive.getPlayer(), SkyWars.getPlugin().getConfig().getInt("reward.death"), true));

                if (killed.isRushMode()) {
                    killed.doRushMode();
                }
            });
        });
    }

}
