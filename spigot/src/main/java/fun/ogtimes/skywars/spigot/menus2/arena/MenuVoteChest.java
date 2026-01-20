package fun.ogtimes.skywars.spigot.menus2.arena;

import fun.ogtimes.skywars.spigot.SkyWars;
import fun.ogtimes.skywars.spigot.arena.Arena;
import fun.ogtimes.skywars.spigot.arena.chest.ChestType;
import fun.ogtimes.skywars.spigot.arena.chest.ChestTypeManager;
import fun.ogtimes.skywars.spigot.menus2.Menu;
import fun.ogtimes.skywars.spigot.player.SkyPlayer;
import fun.ogtimes.skywars.spigot.utils.ItemBuilder;
import fun.ogtimes.skywars.spigot.utils.Messages;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

public class MenuVoteChest extends Menu {
   public MenuVoteChest(Player var1) {
      super(var1, "voteChest", SkyWars.getMessage(Messages.VOTE_CHESTS_TITLE), 3);
   }

   public void onOpen(InventoryOpenEvent var1) {
      this.update();
   }

   public void onClose(InventoryCloseEvent var1) {
   }

   public void onClick(InventoryClickEvent var1) {
      SkyPlayer var2 = SkyWars.getSkyPlayer(this.getPlayer());
      if (var2.isInArena()) {
         Arena var3 = var2.getArena();
         ChestType[] var4 = ChestTypeManager.getChestTypes();
         int var5 = var4.length;

          for (ChestType var7 : var4) {
              if (var1.getCurrentItem().getType() == var7.getItem() && var1.getSlot() == var7.getSlot()) {
                  if (!var2.hasPermissions("skywars.vote.chest." + var7.getName())) {
                      var2.sendMessage(SkyWars.getMessage(Messages.PLAYER_NEEDPERMISSIONS_VOTE_CHEST));
                      this.getPlayer().closeInventory();
                      return;
                  }

                  if (var2.hasData("voted_chest")) {
                      var2.sendMessage(SkyWars.getMessage(Messages.VOTE_ONLY1));
                      this.getPlayer().closeInventory();
                      return;
                  }

                  var2.addData("voted_chest", true);
                  var2.addData("voted_chest_" + var7.getName(), true);
                  var3.addData("vote_chest_" + var7.getName(), var3.getInt("vote_chest_" + var7.getName()) + 1);
                  this.getPlayer().sendMessage(String.format(SkyWars.getMessage(Messages.VOTE_CHESTS_SUCCESSFUL), ChatColor.stripColor(var7.getTitle())));
                  var3.broadcast(String.format(SkyWars.getMessage(Messages.GAME_PLAYER_VOTE_CHESTS), this.getPlayer().getName(), var7.getShortName(), var3.getInt("vote_chest_" + var7.getName())));
                  this.getPlayer().closeInventory();
              }
          }
      }

   }

   public void update() {
      SkyPlayer var1 = SkyWars.getSkyPlayer(this.getPlayer());
      if (var1.isInArena()) {
         Arena var2 = var1.getArena();
         ChestType[] var3 = ChestTypeManager.getChestTypes();
         int var4 = var3.length;

          for (ChestType var6 : var3) {
              if (var2.getConfig().getStringList("chests.selectable").contains(var6.getName())) {
                  ItemBuilder var7 = new ItemBuilder(var6.getItem(), var6.getItemData());
                  var7.setTitle(var6.getTitle()).setLore(var6.getDescription());
                  var7.addLore(String.format(SkyWars.getMessage(Messages.VOTE_VOTES), var2.getInt("vote_chest_" + var6.getName())));
                  this.setItem(var6.getSlot(), var7);
              }
          }
      }

   }
}
