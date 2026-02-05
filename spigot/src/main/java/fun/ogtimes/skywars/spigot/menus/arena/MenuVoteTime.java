package fun.ogtimes.skywars.spigot.menus.arena;

import fun.ogtimes.skywars.spigot.SkyWars;
import fun.ogtimes.skywars.spigot.arena.Arena;
import fun.ogtimes.skywars.spigot.menus.Menu;
import fun.ogtimes.skywars.spigot.player.SkyPlayer;
import fun.ogtimes.skywars.spigot.utils.ItemBuilder;
import fun.ogtimes.skywars.spigot.utils.Messages;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;

public class MenuVoteTime extends Menu {
   public MenuVoteTime(Player player) {
      super(player, "voteTime", SkyWars.getMessage(Messages.VOTE_TIME_TITLE), 3);
   }

   public void onOpen(InventoryOpenEvent event) {
      this.update();
   }

   public void onClose(InventoryCloseEvent event) {
   }

   public void onClick(InventoryClickEvent event) {
      ItemStack item = event.getCurrentItem();
      SkyPlayer skyPlayer = SkyWars.getSkyPlayer(this.getPlayer());
      if (item == null) {
         return;
      }

      if (skyPlayer.isInArena()) {
         Arena arena = skyPlayer.getArena();

         String prev = skyPlayer.getString("voted_time_type");

         if (item.getType() == Material.STAINED_CLAY && item.getDurability() == 4) {
            if (!skyPlayer.hasPermission("skywars.vote.time.day")) {
               skyPlayer.sendMessage(SkyWars.getMessage(Messages.PLAYER_NEEDPERMISSIONS_VOTE_TIME));
               this.getPlayer().closeInventory();
               return;
            }

            if (prev != null && !prev.isEmpty()) {
               if (prev.equalsIgnoreCase("day")) {
                  this.getPlayer().closeInventory();
                  return;
               }
               int c;
               if (prev.equalsIgnoreCase("night")) {
                  c = arena.getInt("vote_time_night");
                  if (c > 0) arena.addData("vote_time_night", c - 1);
               } else if (prev.equalsIgnoreCase("sunset")) {
                  c = arena.getInt("vote_time_sunset");
                  if (c > 0) arena.addData("vote_time_sunset", c - 1);
               } else if (prev.equalsIgnoreCase("day")) {
                  c = arena.getInt("vote_time_day");
                  if (c > 0) arena.addData("vote_time_day", c - 1);
               }
            }

            skyPlayer.addData("voted_time", true);
            skyPlayer.addData("voted_time_type", "day");
            skyPlayer.addData("voted_time_day", true);
            arena.addData("vote_time_day", arena.getInt("vote_time_day") + 1);
            skyPlayer.sendMessage(String.format(SkyWars.getMessage(Messages.VOTE_TIME_SUCCESSFUL), ChatColor.stripColor(SkyWars.getMessage(Messages.VOTE_TIME_DAY_NAME))));
            arena.broadcast(String.format(SkyWars.getMessage(Messages.GAME_PLAYER_VOTE_TIME), this.getPlayer().getName(), SkyWars.getMessage(Messages.SELECTED_TIME_DAY), arena.getInt("vote_time_day")));
            this.getPlayer().closeInventory();
         }

         if (item.getType() == Material.STAINED_CLAY && item.getDurability() == 15) {
            if (!skyPlayer.hasPermission("skywars.vote.time.night")) {
               skyPlayer.sendMessage(SkyWars.getMessage(Messages.PLAYER_NEEDPERMISSIONS_VOTE_TIME));
               this.getPlayer().closeInventory();
               return;
            }

            if (prev != null && !prev.isEmpty()) {
               if (prev.equalsIgnoreCase("night")) {
                  this.getPlayer().closeInventory();
                  return;
               }
               int c;
               if (prev.equalsIgnoreCase("day")) {
                  c = arena.getInt("vote_time_day");
                  if (c > 0) arena.addData("vote_time_day", c - 1);
               } else if (prev.equalsIgnoreCase("sunset")) {
                  c = arena.getInt("vote_time_sunset");
                  if (c > 0) arena.addData("vote_time_sunset", c - 1);
               } else if (prev.equalsIgnoreCase("night")) {
                  c = arena.getInt("vote_time_night");
                  if (c > 0) arena.addData("vote_time_night", c - 1);
               }
            }

            skyPlayer.addData("voted_time", true);
            skyPlayer.addData("voted_time_type", "night");
            skyPlayer.addData("voted_time_night", true);
            arena.addData("vote_time_night", arena.getInt("vote_time_night") + 1);
            skyPlayer.sendMessage(String.format(SkyWars.getMessage(Messages.VOTE_TIME_SUCCESSFUL), ChatColor.stripColor(SkyWars.getMessage(Messages.VOTE_TIME_NIGHT_NAME))));
            arena.broadcast(String.format(SkyWars.getMessage(Messages.GAME_PLAYER_VOTE_TIME), this.getPlayer().getName(), SkyWars.getMessage(Messages.SELECTED_TIME_NIGHT), arena.getInt("vote_time_night")));
            this.getPlayer().closeInventory();
         }

         if (item.getType() == Material.STAINED_CLAY && item.getDurability() == 14) {
            if (!skyPlayer.hasPermission("skywars.vote.time.sunset")) {
               skyPlayer.sendMessage(SkyWars.getMessage(Messages.PLAYER_NEEDPERMISSIONS_VOTE_TIME));
               this.getPlayer().closeInventory();
               return;
            }

            if (prev != null && !prev.isEmpty()) {
               if (prev.equalsIgnoreCase("sunset")) {
                  this.getPlayer().closeInventory();
                  return;
               }
               int c;
               if (prev.equalsIgnoreCase("day")) {
                  c = arena.getInt("vote_time_day");
                  if (c > 0) arena.addData("vote_time_day", c - 1);
               } else if (prev.equalsIgnoreCase("night")) {
                  c = arena.getInt("vote_time_night");
                  if (c > 0) arena.addData("vote_time_night", c - 1);
               } else if (prev.equalsIgnoreCase("sunset")) {
                  c = arena.getInt("vote_time_sunset");
                  if (c > 0) arena.addData("vote_time_sunset", c - 1);
               }
            }

            skyPlayer.addData("voted_time", true);
            skyPlayer.addData("voted_time_type", "sunset");
            skyPlayer.addData("voted_time_sunset", true);
            arena.addData("vote_time_sunset", arena.getInt("vote_time_sunset") + 1);
            skyPlayer.sendMessage(String.format(SkyWars.getMessage(Messages.VOTE_TIME_SUCCESSFUL), ChatColor.stripColor(SkyWars.getMessage(Messages.VOTE_TIME_SUNSET_NAME))));
            arena.broadcast(String.format(SkyWars.getMessage(Messages.GAME_PLAYER_VOTE_TIME), this.getPlayer().getName(), SkyWars.getMessage(Messages.SELECTED_TIME_SUNSET), arena.getInt("vote_time_sunset")));
            this.getPlayer().closeInventory();
         }
      }

   }

   public void update() {
      SkyPlayer skyPlayer = SkyWars.getSkyPlayer(this.getPlayer());
      if (skyPlayer.isInArena()) {
         Arena arena = skyPlayer.getArena();
         this.setItem(10, (new ItemBuilder(Material.STAINED_CLAY, (short)4)).setTitle(SkyWars.getMessage(Messages.VOTE_TIME_DAY_NAME)).addLore(SkyWars.getMessage(Messages.VOTE_TIME_DAY_LORE1)).addLore(String.format(SkyWars.getMessage(Messages.VOTE_TIME_DAY_LORE2), arena.getInt("vote_time_day"))));
         this.setItem(13, (new ItemBuilder(Material.STAINED_CLAY, (short)15)).setTitle(SkyWars.getMessage(Messages.VOTE_TIME_NIGHT_NAME)).addLore(SkyWars.getMessage(Messages.VOTE_TIME_NIGHT_LORE1)).addLore(String.format(SkyWars.getMessage(Messages.VOTE_TIME_NIGHT_LORE2), arena.getInt("vote_time_night"))));
         this.setItem(16, (new ItemBuilder(Material.STAINED_CLAY, (short)14)).setTitle(SkyWars.getMessage(Messages.VOTE_TIME_SUNSET_NAME)).addLore(SkyWars.getMessage(Messages.VOTE_TIME_SUNSET_LORE1)).addLore(String.format(SkyWars.getMessage(Messages.VOTE_TIME_SUNSET_LORE2), arena.getInt("vote_time_sunset"))));
      }

   }
}
