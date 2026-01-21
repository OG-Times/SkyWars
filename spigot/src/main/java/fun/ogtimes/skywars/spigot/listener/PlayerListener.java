package fun.ogtimes.skywars.spigot.listener;

import fun.ogtimes.skywars.spigot.SkyWars;
import fun.ogtimes.skywars.spigot.arena.Arena;
import fun.ogtimes.skywars.spigot.arena.ArenaMode;
import fun.ogtimes.skywars.spigot.arena.ArenaState;
import fun.ogtimes.skywars.spigot.arena.chest.ChestType;
import fun.ogtimes.skywars.spigot.arena.chest.ChestTypeManager;
import fun.ogtimes.skywars.spigot.config.ConfigManager;
import fun.ogtimes.skywars.spigot.player.SkyPlayer;
import fun.ogtimes.skywars.spigot.utils.ItemBuilder;
import fun.ogtimes.skywars.spigot.utils.Messages;
import fun.ogtimes.skywars.spigot.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class PlayerListener implements Listener {
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        SkyPlayer skyPlayer = SkyWars.getSkyPlayer(player);
        if (skyPlayer == null) {
            SkyWars.log("PlayerListener.onBlockBreak - null Player");
            return;
        }

        if (skyPlayer.isInArena()) {
            Arena arena = skyPlayer.getArena();
            if (skyPlayer.isSpectating()) {
                event.setCancelled(true);
            }

            if (arena.getState() == ArenaState.WAITING || arena.getState() == ArenaState.STARTING || arena.getState() == ArenaState.ENDING) {
                event.setCancelled(true);
            }

            if (arena.getState() == ArenaState.INGAME) {
                Block block = event.getBlock();
                Location location = block.getLocation();
                if (block.getState() instanceof Chest chest) {
                    if (!arena.isFilled(location) && !arena.getDontFill().contains(location)) {
                        Inventory inventory = chest.getInventory();
                        ChestType chestType = ChestTypeManager.getChestType(arena.getChest());
                        chestType.fillChest(inventory);
                    }

                    if (!arena.getDontFill().contains(location) && !arena.getOriginalChestLocations().contains(location)) {
                        arena.getOriginalChestLocations().add(location);
                    }

                    arena.removeFilled(location);
                }
            }
        } else {
            event.setCancelled(player.getGameMode() != GameMode.CREATIVE);
        }
    }

    @EventHandler
    public void onChestOpen(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        SkyPlayer skyPlayer = SkyWars.getSkyPlayer(player);
        if (skyPlayer == null) {
            SkyWars.log("PlayerListener.onChestOpen - null Player");
            return;
        }

        if (!skyPlayer.isInArena()) {
            event.setCancelled(player.getGameMode() != GameMode.CREATIVE);
        }

        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
            Block block = event.getClickedBlock();
            Location location = block.getLocation();
            if (block.getState() instanceof Chest chest && skyPlayer.isInArena()) {
                Arena arena = skyPlayer.getArena();
                if (arena.getState() == ArenaState.INGAME && !arena.isFilled(location) && !arena.getDontFill().contains(location)) {
                    arena.addFilled(location);
                    if (!arena.getOriginalChestLocations().contains(location)) {
                        arena.getOriginalChestLocations().add(location);
                    }
                    Inventory inventory = chest.getInventory();
                    ChestType chestType = ChestTypeManager.getChestType(arena.getChest());
                    chestType.fillChest(inventory);
                }
            }
        }
    }

    @EventHandler
    private void onLeavesDecay(LeavesDecayEvent event) {
        if (event.getBlock() == SkyWars.getSpawn().getWorld()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        for (Block block : event.blockList()) {
            if (block.getWorld() == SkyWars.getSpawn().getWorld()) {
                event.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler
    public void onPlaceBlock(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        SkyPlayer skyPlayer = SkyWars.getSkyPlayer(player);
        if (skyPlayer == null) {
            SkyWars.log("PlayerListener.onPlaceBlock - null Player");
            return;
        }

        if (skyPlayer.isInArena()) {
            if (skyPlayer.isSpectating()) {
                event.setCancelled(true);
            }

            Arena arena = skyPlayer.getArena();
            if (arena.getState() == ArenaState.INGAME) {
                Block block = event.getBlock();
                Location location = block.getLocation();
                if (block.getState() instanceof Chest) {
                    arena.getDontFill().add(location);
                    arena.removeFilled(location);
                }
            }
        } else {
            event.setCancelled(player.getGameMode() != GameMode.CREATIVE);
        }

    }

    @EventHandler
    public void onHunger(FoodLevelChangeEvent event) {
        Player player = (Player)event.getEntity();
        SkyPlayer skyPlayer = SkyWars.getSkyPlayer(player);
        if (skyPlayer == null) {
            SkyWars.log("PlayerListener.onHunger - null Player");
            return;
        }

        if (skyPlayer.isInArena()) {
            Arena arena = skyPlayer.getArena();
            if (arena.getState() == ArenaState.WAITING || arena.getState() == ArenaState.STARTING || arena.getState() == ArenaState.ENDING) {
                event.setCancelled(true);
            }

            if (skyPlayer.isSpectating()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        SkyPlayer skyPlayer = SkyWars.getSkyPlayer(player);
        if (skyPlayer == null) {
            SkyWars.log("PlayerListener.onMove - null Player");
            return;
        }

        if (skyPlayer.isInArena() && !player.isDead()) {
            Arena arena = skyPlayer.getArena();
            if (arena == null) {
                return;
            }

            if (skyPlayer.getPlayer().getWorld() == Bukkit.getWorlds().getFirst()) {
                skyPlayer.teleport(arena.getSpawn());
            }

            if (arena.getState() != ArenaState.INGAME && arena.getState() != ArenaState.ENDING) {
                if (arena.getArenaMode() == ArenaMode.TEAM) {
                    if (arena.getState() == ArenaState.WAITING && (player.getLocation().getY() <= 0.0D || !player.getWorld().equals(arena.getTeamLobby().getWorld()))) {
                        skyPlayer.teleport(arena.getTeamLobby());
                    }

                    if (arena.getState() == ArenaState.STARTING && player.getWorld().equals(arena.getWorld()) && player.getLocation().distanceSquared(skyPlayer.getArenaSpawn()) >= 2.0D) {
                        skyPlayer.teleport(skyPlayer.getArenaSpawn());
                    }

                    return;
                }

                if (player.getWorld().equals(arena.getWorld()) && player.getLocation().distanceSquared(skyPlayer.getArenaSpawn()) >= 2.0D && (arena.getState() == ArenaState.WAITING || arena.getState() == ArenaState.STARTING)) {
                    skyPlayer.teleport(skyPlayer.getArenaSpawn());
                }
                return;
            }

            if (!player.getWorld().equals(arena.getWorld())) {
                skyPlayer.teleport(skyPlayer.getArenaSpawn());
            }

            if (player.getLocation().getY() <= 0.0D && (skyPlayer.isSpectating() || arena.getState() == ArenaState.ENDING)) {
                skyPlayer.teleport(arena.getSpawn());
            }

            if (player.getLocation().getY() <= -6.0D) {
                if (DamageListener.lastDamage.containsKey(player.getUniqueId())) {
                    Player lastDamager = Bukkit.getPlayer(DamageListener.lastDamage.get(player.getUniqueId()));
                    player.damage(1000.0D, lastDamager);
                } else {
                    player.setHealth(0.0D);
                }

                skyPlayer.teleport(arena.getSpawn());
            }
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        SkyPlayer skyPlayer = SkyWars.getSkyPlayer(player);
        if (skyPlayer == null) {
            SkyWars.log("PlayerListener.onDrop - null Player");
            return;
        }

        if (skyPlayer.isInArena()) {
            Arena arena = skyPlayer.getArena();
            if (arena.getState() == ArenaState.WAITING || arena.getState() == ArenaState.STARTING) {
                event.setCancelled(true);
            }

            if (skyPlayer.isSpectating()) {
                event.setCancelled(true);
            }
            return;
        }

        if (ConfigManager.shop.getBoolean("item.enabled")) {
            ItemStack droppedItem = event.getItemDrop().getItemStack();
            ItemBuilder itemBuilder = Utils.readItem(ConfigManager.shop.getString("item.item"));
            itemBuilder.setTitle(ConfigManager.shop.getString("item.name")).setLore(ConfigManager.shop.getStringList("item.lore"));
            if (droppedItem.isSimilar(itemBuilder.build())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPickUp(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        SkyPlayer skyPlayer = SkyWars.getSkyPlayer(player);
        if (skyPlayer == null) {
            SkyWars.log("PlayerListener.onPickUp - null Player");
            return;
        }

        if (skyPlayer.isInArena()) {
            Arena arena = skyPlayer.getArena();
            if (arena.getState() == ArenaState.WAITING || arena.getState() == ArenaState.STARTING || arena.getState() == ArenaState.ENDING) {
                event.setCancelled(true);
            }

            if (skyPlayer.isSpectating()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onAnimation(BlockDamageEvent event) {
        Player player = event.getPlayer();
        SkyPlayer skyPlayer = SkyWars.getSkyPlayer(player);
        if (skyPlayer == null) {
            SkyWars.log("PlayerListener.onAnimation - null Player");
            return;
        }

        if (skyPlayer.isInArena()) {
            Arena arena = skyPlayer.getArena();
            if (arena.getState() == ArenaState.WAITING || arena.getState() == ArenaState.STARTING) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        SkyPlayer skyPlayer = SkyWars.getSkyPlayer(player);
        if (skyPlayer == null) {
            SkyWars.log("PlayerListener.onChat - null Player");
            return;
        }

        if (skyPlayer.isInArena()) {
            Arena arena = skyPlayer.getArena();
            if ((arena.getState() == ArenaState.WAITING || arena.getState() == ArenaState.STARTING) && !player.hasPermission("skywars.vip.talk")) {
                event.setCancelled(true);
                skyPlayer.sendMessage(SkyWars.getMessage(Messages.GAME_PLAYER_TALK));
            }
        }

        if (SkyWars.isMultiArenaMode() && !ConfigManager.main.getBoolean("options.disablePerWorldChat")) {

            for (Player online : Bukkit.getServer().getOnlinePlayers()) {
                if (online.getWorld() != player.getWorld()) {
                    event.getRecipients().remove(online);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        SkyPlayer skyPlayer = SkyWars.getSkyPlayer(player);
        if (skyPlayer != null) {
            if (skyPlayer.isInArena()) {
                String[] message = event.getMessage().split(" ");
                message[0] = message[0].replace("/", "");
                if (ConfigManager.main.getString("block.commands.mode").equalsIgnoreCase("blacklist")) {
                    if (ConfigManager.main.getStringList("block.commands.ingame").contains(message[0])) {
                        skyPlayer.sendMessage(SkyWars.getMessage(Messages.GAME_PLAYER_BLOCKEDCOMMANDS));
                        event.setCancelled(true);
                    }

                } else {
                    if (message[0].equalsIgnoreCase("salir") ||
                            message[0].equalsIgnoreCase("leave") ||
                            message[0].equalsIgnoreCase("playagain")
                    ) return;

                    if (!ConfigManager.main.getStringList("block.commands.ingame").contains(message[0])) {
                        skyPlayer.sendMessage(SkyWars.getMessage(Messages.GAME_PLAYER_BLOCKEDCOMMANDS));
                        event.setCancelled(true);
                    }
                }
            }

        }
    }
}
