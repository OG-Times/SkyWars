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
    public void onSkyPlayerSpectator(SkyPlayerSpectatorEvent event) {
        SkyPlayer player = event.getPlayer();
        Arena arena = event.getGame();
        if (!event.isLeaveReason() && event.getSpectate()) {
            if (event.getSpectate() && event.isDeathReason()) {
                if (ConfigManager.main.getBoolean("options.disableSpectatorMode-Death")) {
                    if (SkyWars.isProxyMode()) {
                        arena.removePlayer(player, ArenaLeaveCause.SPECTATOR_DISABLED_ON_DEATH);
                        ProxyUtils.teleToServer(player.getPlayer(), SkyWars.getMessage(Messages.PLAYER_TELEPORT_LOBBY), SkyWars.getRandomLobby());
                    } else {
                        arena.removePlayer(player, ArenaLeaveCause.SPECTATOR_DISABLED_ON_DEATH);
                    }

                    return;
                }

                if (player.getPlayer().getGameMode() != GameMode.SPECTATOR) {
                    player.getPlayer().setGameMode(GameMode.SPECTATOR);
                }
            }

            if (event.getSpectate() && event.isJoinReason()) {
                if (player.getPlayer().getGameMode() != GameMode.SPECTATOR) {
                    player.getPlayer().setGameMode(GameMode.SPECTATOR);
                    player.sendMessage(SkyWars.getMessage(Messages.PLAYER_DEATH));
                }
            }

        } else {
            if (player.getPlayer().getGameMode() != GameMode.SURVIVAL) {
                player.getPlayer().setGameMode(GameMode.SURVIVAL);
            }

        }
    }
}
