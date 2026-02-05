package fun.ogtimes.skywars.spigot.listener.skywars;

import fun.ogtimes.skywars.spigot.SkyWars;
import fun.ogtimes.skywars.spigot.arena.Arena;
import fun.ogtimes.skywars.spigot.arena.ArenaBox;
import fun.ogtimes.skywars.spigot.arena.ArenaManager;
import fun.ogtimes.skywars.spigot.arena.ArenaMode;
import fun.ogtimes.skywars.spigot.arena.ArenaState;
import fun.ogtimes.skywars.spigot.arena.GameQueue;
import fun.ogtimes.skywars.spigot.arena.event.ArenaEvent;
import fun.ogtimes.skywars.spigot.config.ConfigManager;
import fun.ogtimes.skywars.spigot.events.ArenaJoinEvent;
import fun.ogtimes.skywars.spigot.events.ArenaLeaveEvent;
import fun.ogtimes.skywars.spigot.events.ArenaTickEvent;
import fun.ogtimes.skywars.spigot.events.enums.ArenaLeaveCause;
import fun.ogtimes.skywars.spigot.events.enums.SpectatorReason;
import fun.ogtimes.skywars.spigot.player.SkyPlayer;
import fun.ogtimes.skywars.spigot.server.SkyServer;
import fun.ogtimes.skywars.spigot.utils.ProxyUtils;
import fun.ogtimes.skywars.spigot.utils.ItemBuilder;
import fun.ogtimes.skywars.spigot.utils.Messages;
import fun.ogtimes.skywars.spigot.utils.sky.SkyHologram;
import fun.ogtimes.skywars.spigot.utils.title.Title;
import java.util.Iterator;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

public class ArenaListener implements Listener {
    public static boolean checkWinner(Arena arena) {
        if (arena.getAlivePlayers() <= 1) {
            if (arena.getAlivePlayers() == 0) {
                arena.end(true);
            } else {
                SkyPlayer skyPlayer = arena.getAlivePlayer().getFirst();
                if (skyPlayer != null) {
                    Player player = skyPlayer.getPlayer();
                    if (player != null) {
                        Location location = player.getLocation();
                        if (location != null) {
                            player.playSound(location, Sound.valueOf("ORB_PICKUP"), 1.0F, 1.0F);
                        }
                    }

                    arena.end(skyPlayer);
                } else {
                    arena.end(true);
                }

            }
            return true;
        } else {
            return false;
        }
    }

    @EventHandler
    public void onSkyPlayerArenaJoinEvent(ArenaJoinEvent var1) {
        SkyPlayer skyPlayer = var1.getPlayer();
        Arena arena = var1.getGame();

        if (arena.getState() == ArenaState.INGAME && skyPlayer.getPlayer().hasPermission("skywars.admin.spectate")) {
            skyPlayer.clearInventory(true);
            skyPlayer.setArena(arena);
            skyPlayer.teleport(arena.getSpawn());
            skyPlayer.setSpectating(true, SpectatorReason.JOIN);
            SkyHologram.removeHologram(skyPlayer);
        } else {
            if (arena.getArenaMode() == ArenaMode.SOLO) {
                Location location = arena.acquireSpawn();
                SkyWars.log("Arena.addPlayer - Acquire Spawn " + location);

                if (location == null) {
                    // Ultra fallback: no hay spawns configurados.
                    SkyWars.logError("Arena.addPlayer - No spawnpoints configured for arena '" + arena.getName() + "'. Using spectator/world spawn.");
                    location = arena.getSpawn();
                }

                for (ArenaBox arenaBox : arena.getGlassBoxes()) {
                    Location boxLocation = arenaBox.getLocation();
                    if (boxLocation.equals(location)) {
                        SkyWars.log("Arena.addPlayer - Selected box - " + boxLocation);
                        skyPlayer.setBox(arenaBox);
                    }
                }

                SkyWars.log("Arena.addPlayer - " + skyPlayer.getName() + " is teleporting to " + location);
                arena.setUsed(location, true);
                skyPlayer.setArenaSpawn(location);
                skyPlayer.teleport(location);
                String var11 = skyPlayer.getBoxSection();
                if (skyPlayer.getBoxSection() != null && !var11.equalsIgnoreCase(SkyWars.boxes.getString("default"))) {
                    int boxData;
                    ArenaBox box;
                    String boxSection;
                    int boxItem;
                    if (skyPlayer.getBoxItem(skyPlayer.getBoxSection()) != 0) {
                        boxSection = skyPlayer.getBoxSection();
                    } else {
                        skyPlayer.getPlayer().setMetadata("upload_me", new FixedMetadataValue(SkyWars.getPlugin(), true));
                        boxSection = SkyWars.boxes.getString("default");
                        skyPlayer.setBoxSection(boxSection, true);
                    }
                    boxItem = skyPlayer.getBoxItem(boxSection);
                    boxData = skyPlayer.getBoxData(boxSection);
                    box = skyPlayer.getBox();
                    SkyWars.log("Arena.addPlayer - Box Section=" + boxSection + ", Box Item=" + boxItem + ", Box Data=" + boxData + ", Box=" + box);
                    box.setBox(boxItem, boxData);
                }
            } else {
                skyPlayer.teleport(arena.getTeamLobby());
            }

            skyPlayer.clearInventory(true);
            skyPlayer.setArena(arena);
            SkyWars.log("Arena.addPlayer - Player already in list: " + arena.getPlayers().contains(skyPlayer));
            if (!arena.getPlayers().contains(skyPlayer)) {
                arena.getPlayers().add(skyPlayer);
                SkyWars.log("Arena.addPlayer - Player add in list");
            }

            if (skyPlayer.getPlayer().getGameMode() != GameMode.SURVIVAL) {
                skyPlayer.getPlayer().setGameMode(GameMode.SURVIVAL);
            }

            for (SkyPlayer var12 : arena.getPlayers()) {
                Player var14 = var12.getPlayer();
                Player var16 = skyPlayer.getPlayer();
                if (var14 != null && var16 != null && var12 != skyPlayer) {
                    var14.showPlayer(var16);
                }
            }

            this.setInventoryItems(skyPlayer, arena);
            SkyWars.log("Arena.addPlayer - Successfull add " + skyPlayer.getName() + " to " + skyPlayer.getArena().getName());
            arena.broadcast(String.format(SkyWars.getMessage(Messages.GAME_PLAYER_JOIN), skyPlayer.getName(), arena.getAlivePlayers(), arena.getMaxPlayers()));
            skyPlayer.setSpectating(false, SpectatorReason.JOIN);
            SkyHologram.removeHologram(skyPlayer);
            GameQueue.removePlayer(skyPlayer);
        }
    }

    private void setInventoryItems(SkyPlayer var1, Arena var2) {
        PlayerInventory var3 = var1.getPlayer().getInventory();
        var3.clear();
        String[] var4 = ConfigManager.main.getString("item.kits").split(" ");
        Material var5 = Material.PAPER;
        int var6 = 1;
        if (var4.length >= 2) {
            try {
                var6 = Integer.parseInt(var4[0]);
                var5 = Material.matchMaterial(var4[1]);
            } catch (NumberFormatException var20) {
                SkyWars.logError("Wrong item format in config.yml (item.kits)");
            }
        }

        String[] var7 = ConfigManager.main.getString("item.settings").split(" ");
        Material var8 = Material.DIAMOND;
        int var9 = 2;
        if (var7.length >= 2) {
            try {
                var9 = Integer.parseInt(var7[0]);
                var8 = Material.matchMaterial(var7[1]);
            } catch (NumberFormatException var19) {
                SkyWars.logError("Wrong item format in config.yml (item.settings)");
            }
        }

        String[] var10 = ConfigManager.main.getString("item.vote").split(" ");
        Material var11 = Material.EMPTY_MAP;
        int var12 = 3;
        if (var10.length >= 2) {
            try {
                var12 = Integer.parseInt(var10[0]);
                var11 = Material.matchMaterial(var10[1]);
            } catch (NumberFormatException var18) {
                SkyWars.logError("Wrong item format in config.yml (item.vote)");
            }
        }

        String[] var13 = ConfigManager.main.getString("item.exit").split(" ");
        Material var14 = Material.BED;
        int var15 = 9;
        if (var13.length >= 2) {
            try {
                var15 = Integer.parseInt(var13[0]);
                var14 = Material.matchMaterial(var13[1]);
            } catch (NumberFormatException var17) {
                SkyWars.logError("Wrong item format in config.yml (item.exit)");
            }
        }

        var3.setItem(var6 - 1, (new ItemBuilder(var5)).setTitle(SkyWars.getMessage(Messages.ITEM_KITS_NAME)).addLore(SkyWars.getMessage(Messages.ITEM_KITS_LORE)).build());
        var3.setItem(var9 - 1, (new ItemBuilder(var8)).setTitle(SkyWars.getMessage(Messages.ITEM_SETTINGS_NAME)).addLore(SkyWars.getMessage(Messages.ITEM_SETTINGS_LORE)).build());
        if ((var2.getConfig().getBoolean("options.vote.chest") || var2.getConfig().getBoolean("options.vote.time")) && var2.getArenaMode() == ArenaMode.SOLO) {
            var3.setItem(var12 - 1, (new ItemBuilder(var11)).setTitle(SkyWars.getMessage(Messages.ITEM_VOTE_NAME)).addLore(SkyWars.getMessage(Messages.ITEM_VOTE_LORE)).build());
        }

        if (!SkyWars.getPlugin().getConfig().getBoolean("options.disableLeaveItem")) {
            var3.setItem(var15 - 1, (new ItemBuilder(var14)).setTitle(SkyWars.getMessage(Messages.ITEM_SPECTATOR_EXIT_NAME)).build());
        }

        var1.updateInventory();
    }

    @EventHandler
    public void onSkyPlayerArenaLeaveEvent(ArenaLeaveEvent var1) {
        SkyPlayer skyPlayer = var1.getPlayer();
        Arena arena = var1.getGame();
        Player var4 = skyPlayer.getPlayer();
        if (!skyPlayer.isSpectating() && var1.getCause() != ArenaLeaveCause.RESTART) {
            arena.broadcast(String.format(SkyWars.getMessage(Messages.GAME_PLAYER_QUIT), skyPlayer.getName(), arena.getAlivePlayers() - 1, arena.getMaxPlayers()));
        }

        skyPlayer.setSpectating(false, SpectatorReason.LEAVE);
        SkyWars.log("Arena.removePlayer - Removing to " + skyPlayer.getName() + " from " + arena.getName() + " cause: " + var1.getCause());
        if (var4.isOnline()) {
            arena.getPlayers().remove(skyPlayer);
            SkyWars.log("Arena.removePlayer - Successful remove to " + skyPlayer.getName() + " from " + arena.getName());
        }

        String var5 = SkyWars.boxes.getString("default");
        if (skyPlayer.getBox() != null) {
            skyPlayer.getBox().setBox(skyPlayer.getBoxItem(var5), skyPlayer.getBoxData(var5));
        }

        arena.resetPlayer(skyPlayer);
        if (!SkyWars.isProxyMode()) {
            skyPlayer.upload(false);
            SkyWars.goToSpawn(skyPlayer);
            var4.setFallDistance(0.0F);
        }

    }

    @EventHandler
    public void onArenaTick(ArenaTickEvent var1) {
        Arena var2 = var1.getArena();
        SkyServer.setValues(var2);
        if (var2.getState() == ArenaState.INGAME) {
            if (checkWinner(var2)) {
                return;
            }

            if (!var2.isFallDamage()) {
                if (var2.getStartCountdown() == -5) {
                    var2.setFallDamage(true);
                }

                var2.setStartCountdown(var2.getStartCountdown() - 1);
            }

            this.countEvents(var2);
            this.countMaxTime(var2);
        }

        if (var2.getState() == ArenaState.WAITING || var2.getState() == ArenaState.STARTING) {
            int var3 = var2.getStartCountdown();
            if (this.checkEmpty(var2)) {
                return;
            }

            if (var3 == 0) {
                if (var2.getPlayers().size() < var2.getMinPlayers() && !var2.isForceStart()) {
                    var2.setStartCountdown(var2.getStartFullCountdown());
                    var2.broadcast(SkyWars.getMessage(Messages.GAME_START_NOREQUIREDPLAYERS));
                    return;
                }

                if (var2.getState() == ArenaState.STARTING) {
                    var2.start();

                    for (SkyPlayer var5 : var2.getPlayers()) {
                        var5.getPlayer().setLevel(0);
                    }

                    return;
                }
            }

            if (var2.getState() == ArenaState.WAITING) {
                if (var2.getPlayers().size() < var2.getMinPlayers() && !var2.isForceStart()) {
                    return;
                }

                if (var2.isForceStart()) {
                    var2.setStartCountdown(var2.getStartFullCountdown());
                    var3 = var2.getStartCountdown();
                } else if (var2.getPlayers().size() >= var2.getMaxPlayers()) {
                    if (var3 > var2.getStartFullCountdown()) {
                        var2.setStartCountdown(var2.getStartFullCountdown());
                        var3 = var2.getStartCountdown();
                    }

                    var2.broadcast(String.format(SkyWars.getMessage(Messages.GAME_START_NOWFULL), var3));
                }

                if (var3 <= var2.getStartFullCountdown()) {
                    var2.setState(ArenaState.STARTING);
                }
            }

            this.countStart(var2);
        }

        if (var2.getState() == ArenaState.ENDING) {
            this.countEnd(var2);
        }

    }

    private void countMaxTime(Arena var1) {
        int var2 = var1.getMaxTimeCountdown();
        if (var2 % 60 == 0 && var2 <= 300 && var2 > 0) {
            var1.broadcast(String.format(SkyWars.getMessage(Messages.GAME_TIME_LEFT_MINUTES), var2 % 3600 / 60));
        }

        if ((var2 == 10 || var2 <= 5) && var2 > 0) {
            var1.broadcast(String.format(SkyWars.getMessage(Messages.GAME_TIME_LEFT_SECONDS), var2));
        }

        if (var2 == 0) {
            var1.broadcast(SkyWars.getMessage(Messages.GAME_TIME_LIMIT));
            var1.setState(ArenaState.ENDING);
            var1.end(true);
        }

        var1.setMaxTimeCountdown(var1.getMaxTimeCountdown() - 1);
    }

    private void countStart(Arena arena) {
        int countdown = arena.getStartCountdown();

        for (SkyPlayer skyPlayer : arena.getPlayers()) {
            if (countdown >= 0) {
                Player player = skyPlayer.getPlayer();
                if (player != null) {
                    player.setLevel(countdown);
                }
            }
        }

        if (arena.getStartingCounts().contains(countdown)) {
            if (countdown >= 60) {
                int minutes = countdown % 3600 / 60;
                int seconds = countdown % 60;
                if (seconds == 0) {
                    arena.broadcast(String.format(SkyWars.getMessage(Messages.GAME_START_COUNTDOWN_MINUTES), minutes));
                } else {
                    arena.broadcast(String.format(SkyWars.getMessage(Messages.GAME_START_COUNTDOWN_MINUTES_SECONDS), minutes, seconds));
                }
            } else {
                arena.getPlayers().forEach((skyPlayer) -> {
                    Player player = skyPlayer.getPlayer();
                    if (player != null) {
                        player.playSound(player.getLocation(), Sound.valueOf("CLICK"), 1.0F, 1.0F);

                        if (countdown <= 5) {
                            Title title = new Title(String.format(SkyWars.getMessage(Messages.GAME_START_COUNTDOWN_ALERT), countdown), 0, 25, 0);
                            title.send(player);
                        }

                    }
                });
                if (countdown == 1) {
                    arena.broadcast(String.format(SkyWars.getMessage(Messages.GAME_START_COUNTDOWN_ONE), countdown));
                } else {
                    arena.broadcast(String.format(SkyWars.getMessage(Messages.GAME_START_COUNTDOWN), countdown));
                }
            }
        }

        arena.setStartCountdown(countdown - 1);
    }

    private void countEnd(Arena var1) {
        int var2 = var1.getEndCountdown();
        Iterator var3;
        Player var4;
        if (var2 == 0) {
            if (SkyWars.isProxyMode()) {
                var3 = Bukkit.getOnlinePlayers().iterator();

                while(var3.hasNext()) {
                    var4 = (Player)var3.next();
                    ProxyUtils.teleToServer(var4, SkyWars.getMessage(Messages.PLAYER_TELEPORT_LOBBY), SkyWars.getRandomLobby());
                }
            } else {
                var1.restart();
            }
        }

        if (var2 == -3 && SkyWars.isProxyMode()) {
            if (SkyWars.isAutoStart()) {
                var3 = Bukkit.getOnlinePlayers().iterator();

                while(var3.hasNext()) {
                    var4 = (Player)var3.next();
                    var4.kickPlayer(SkyWars.getMessage(Messages.GAME_RESTART));
                }

                if (SkyWars.isRandomMap()) {
                    var3 = var1.getTimers().iterator();

                    while(var3.hasNext()) {
                        BukkitRunnable var5 = (BukkitRunnable)var3.next();
                        var5.cancel();
                    }

                    var1.getTicks().cancel();
                    ArenaManager.initGames();
                } else {
                    var1.restart();
                }
            } else {
                Bukkit.shutdown();
            }
        }

        var1.setEndCountdown(var1.getEndCountdown() - 1);
    }

    private void countEvents(Arena arena) {
        if (!arena.getEvents().isEmpty() && arena.getConfig().getBoolean("options.events") && arena.getState() == ArenaState.INGAME) {
            ArenaEvent event = arena.getEvents().getFirst();
            event.setSeconds(event.getSeconds() - 1);
            if (event.getSeconds() <= 0) {
                event.playEvent(arena);
                arena.getEvents().removeFirst();
            }

        }
    }

    private boolean checkEmpty(Arena var1) {
        if (var1.getPlayers().size() <= 1) {
            var1.setStartCountdown(var1.getConfig().getInt("countdown.starting"));
            var1.setForceStart(false);
            if (var1.getState() != ArenaState.WAITING) {
                var1.setState(ArenaState.WAITING);
            }

            return true;
        } else {
            return false;
        }
    }
}
