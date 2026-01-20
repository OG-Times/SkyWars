package fun.ogtimes.skywars.spigot.listener;

import fun.ogtimes.skywars.spigot.SkyWars;
import fun.ogtimes.skywars.spigot.arena.Arena;
import fun.ogtimes.skywars.spigot.arena.ArenaManager;
import fun.ogtimes.skywars.spigot.arena.GameQueue;
import fun.ogtimes.skywars.spigot.config.ConfigManager;
import fun.ogtimes.skywars.spigot.events.enums.ArenaJoinCause;
import fun.ogtimes.skywars.spigot.events.enums.ArenaLeaveCause;
import fun.ogtimes.skywars.spigot.menus2.arena.MenuKitSelector;
import fun.ogtimes.skywars.spigot.menus2.arena.MenuSettings;
import fun.ogtimes.skywars.spigot.menus2.arena.MenuTracker;
import fun.ogtimes.skywars.spigot.menus2.arena.MenuVote;
import fun.ogtimes.skywars.spigot.menus2.lobby.MenuShop;
import fun.ogtimes.skywars.spigot.player.SkyPlayer;
import fun.ogtimes.skywars.spigot.sign.SignManager;
import fun.ogtimes.skywars.spigot.sign.SkySign;
import fun.ogtimes.skywars.spigot.utils.ItemBuilder;
import fun.ogtimes.skywars.spigot.utils.Utils;
import fun.ogtimes.skywars.spigot.utils.sky.SkyHologram;
import java.util.Date;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;

public class LoginListener implements Listener {
    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onAsnycPrePlayerLogin(AsyncPlayerPreLoginEvent var1) {
        if (!SkyWars.login) {
            var1.disallow(Result.KICK_OTHER, "Try later... the server is loading");
        }

    }

    @EventHandler
    public void onPlayerTimeLogin(PlayerLoginEvent var1) {
        long var2 = SkyWars.seconds;
        long var4 = (new Date()).getTime();
        long var6 = var4 - var2;
        if (var6 < 1500L) {
            var1.setKickMessage("Try later... the server is loading");
            var1.setResult(PlayerLoginEvent.Result.KICK_OTHER);
        }

    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent var1) {
        if (!SkyWars.firstJoin && !SkyWars.isProxyMode()) {
            SkyWars.firstJoin = true;
            Bukkit.getScheduler().runTaskTimer(SkyWars.getPlugin(), () -> {

                for (SkySign sign : SignManager.getSigns()) {
                    SignManager.updateSign(sign);
                }

            }, 20L, 6000L);
        }

        Player player = var1.getPlayer();
        if (Bukkit.getScoreboardManager().getMainScoreboard() != null && Bukkit.getScoreboardManager().getMainScoreboard() == player.getScoreboard()) {
            player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        }

        if (player.getScoreboard() == null) {
            player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        }

        SkyPlayer var3 = new SkyPlayer(player.getName(), player.getUniqueId());
        SkyWars.skyPlayers.put(player.getName(), var3);
        SkyWars.skyPlayersUUID.put(player.getUniqueId(), var3);
        if (SkyWars.isProxyMode() && ArenaManager.getGames().size() > 0) {
            Arena var4 = (Arena)ArenaManager.getGames().iterator().next();
            if (var4 != null) {
                var4.addPlayer(var3, ArenaJoinCause.LOGIN);
            } else {
                SkyWars.goToSpawn(var3);
            }
        }

        if (SkyWars.isProxyMode() && !player.hasPlayedBefore()) {
            SkyWars.goToSpawn(var3);
        }

        Bukkit.getScheduler().runTaskLater(SkyWars.getPlugin(), () -> {
            if ((SkyWars.isMultiArenaMode() && !var3.isInArena() || SkyWars.isLobbyMode()) && ConfigManager.shop.getBoolean("item.enabled")) {
                ItemBuilder var2x = Utils.readItem(ConfigManager.shop.getString("item.item"));
                var2x.setTitle(ConfigManager.shop.getString("item.name")).setLore(ConfigManager.shop.getStringList("item.lore"));
                player.getInventory().setItem(ConfigManager.shop.getInt("item.inventorySlot"), var2x.build());
            }

        }, 5L);
        new MenuKitSelector(player);
        new MenuTracker(player);
        new MenuVote(player);
        new MenuSettings(player);
        new MenuShop(player);
        if (SkyWars.getUpdate() && player.hasPermission("skywars.admin")) {
            player.sendMessage(SkyWars.checkUpdate());
        }

        if ((SkyWars.isMultiArenaMode() || SkyWars.isLobbyMode()) && SkyWars.getPlugin().getConfig().getBoolean("options.forceLobbySpawn")) {
            SkyWars.goToSpawn(var3);
        }

    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent var1) {
        if (!SkyWars.getPlugin().getConfig().getBoolean("options.leaveMessage")) {
            var1.setQuitMessage((String)null);
        }

        Player var2 = var1.getPlayer();
        var2.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        SkyPlayer var3 = SkyWars.getSkyPlayer(var2);
        if (var3 != null) {
            GameQueue.removePlayer(var3);
            if (var3.isInArena()) {
                if (DamageListener.lastDamage.containsKey(var2.getUniqueId())) {
                    Player var4 = Bukkit.getPlayer((UUID)DamageListener.lastDamage.get(var2.getUniqueId()));
                    var2.damage(1000.0D, var4);
                    var3.addDeaths(1);
                }

                Arena var5 = var3.getArena();
                var5.removePlayer(var3, ArenaLeaveCause.LEAVE);
                SkyWars.log("LoginListener.onPlayerLeave - " + var3.getName() + " removed in quit");
            }

            var3.upload(false);
            SkyHologram.removeHologram(var3);
            SkyWars.skyPlayers.remove(var2.getName());
            SkyWars.skyPlayersUUID.remove(var2.getUniqueId());
        }
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent var1) {
        if (!SkyWars.getPlugin().getConfig().getBoolean("options.leaveMessage")) {
            var1.setLeaveMessage((String)null);
        }

        Player var2 = var1.getPlayer();
        SkyPlayer var3 = SkyWars.getSkyPlayer(var2);
        if (var3 != null) {
            GameQueue.removePlayer(var3);
            if (var3.isInArena()) {
                if (DamageListener.lastDamage.containsKey(var2.getUniqueId())) {
                    Player var4 = Bukkit.getPlayer((UUID)DamageListener.lastDamage.get(var2.getUniqueId()));
                    var2.damage(1000.0D, var4);
                    var3.addDeaths(1);
                }

                Arena var5 = var3.getArena();
                var5.removePlayer(var3, ArenaLeaveCause.KICK);
                SkyWars.log("LoginListener.onPlayerKick - " + var3.getName() + " removed in kick");
            }

            var3.upload(false);
            SkyHologram.removeHologram(var3);
            SkyWars.skyPlayers.remove(var2.getName());
            SkyWars.skyPlayersUUID.remove(var2.getUniqueId());
        }
    }

    @EventHandler(
            priority = EventPriority.MONITOR
    )
    public void disableJoinMessage(PlayerJoinEvent var1) {
        if (!SkyWars.getPlugin().getConfig().getBoolean("options.joinMessage")) {
            var1.setJoinMessage((String)null);
        }

    }

    @EventHandler(
            priority = EventPriority.MONITOR
    )
    public void disableQuitMessage(PlayerQuitEvent var1) {
        if (!SkyWars.getPlugin().getConfig().getBoolean("options.leaveMessage")) {
            var1.setQuitMessage((String)null);
        }

    }
}
