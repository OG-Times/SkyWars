package fun.ogtimes.skywars.spigot.menus.arena;

import fun.ogtimes.skywars.spigot.SkyWars;
import fun.ogtimes.skywars.spigot.arena.Arena;
import fun.ogtimes.skywars.spigot.menus.Menu;
import fun.ogtimes.skywars.spigot.menus.MenuListener;
import fun.ogtimes.skywars.spigot.player.SkyPlayer;
import fun.ogtimes.skywars.spigot.utils.ItemBuilder;
import fun.ogtimes.skywars.spigot.utils.Messages;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

public class MenuVote extends Menu {
   public MenuVote(Player var1) {
      super(var1, "vote", SkyWars.getMessage(Messages.VOTE_MENU_TITLE), 3);
      new MenuVoteChest(var1);
      new MenuVoteTime(var1);
   }

   public void onOpen(InventoryOpenEvent event) {
      this.update();
   }

   public void onClose(InventoryCloseEvent event) {
   }

   public void onClick(InventoryClickEvent event) {
      if (event.getCurrentItem().getType() == Material.CHEST) {
         this.getPlayer().openInventory(MenuListener.getPlayerMenu(this.getPlayer(), "voteChest").getInventory());
      }

      if (event.getCurrentItem().getType() == Material.WATCH) {
         this.getPlayer().openInventory(MenuListener.getPlayerMenu(this.getPlayer(), "voteTime").getInventory());
      }

   }

   public void update() {
      SkyPlayer var1 = SkyWars.getSkyPlayer(this.getPlayer());
      if (var1.isInArena()) {
         Arena var2 = var1.getArena();
         if (var2.getConfig().getBoolean("options.vote.chest")) {
            this.setItem(11, (new ItemBuilder(Material.CHEST)).setTitle(SkyWars.getMessage(Messages.VOTE_CHESTS_NAME)).addLore(SkyWars.getMessage(Messages.VOTE_CHESTS_LORE)));
         }

         if (var2.getConfig().getBoolean("options.vote.time")) {
            this.setItem(15, (new ItemBuilder(Material.WATCH)).setTitle(SkyWars.getMessage(Messages.VOTE_TIME_NAME)).addLore(SkyWars.getMessage(Messages.VOTE_TIME_LORE)));
         }
      }

   }
}
