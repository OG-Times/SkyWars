package fun.ogtimes.skywars.spigot.menus2.arena;

import fun.ogtimes.skywars.spigot.SkyWars;
import fun.ogtimes.skywars.spigot.config.ConfigManager;
import fun.ogtimes.skywars.spigot.kit.Kit;
import fun.ogtimes.skywars.spigot.kit.KitManager;
import fun.ogtimes.skywars.spigot.menus2.Menu;
import fun.ogtimes.skywars.spigot.player.SkyPlayer;
import fun.ogtimes.skywars.spigot.utils.Console;
import fun.ogtimes.skywars.spigot.utils.ItemBuilder;
import fun.ogtimes.skywars.spigot.utils.Messages;
import fun.ogtimes.skywars.spigot.utils.Utils;
import fun.ogtimes.skywars.spigot.utils.economy.SkyEconomyManager;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

public class MenuKitSelector extends Menu {
   public MenuKitSelector(Player var1) {
      super(var1, "kitSelector", SkyWars.getMessage(Messages.KITS_MENU_TITLE), ConfigManager.main.getInt("kitmenu_rows"));
   }

   public void onOpen(InventoryOpenEvent var1) {
      this.update();
   }

   public void onClose(InventoryCloseEvent var1) {
   }

   public void onClick(InventoryClickEvent var1) {
      SkyPlayer var2 = SkyWars.getSkyPlayer(this.getPlayer());
      if (var2 != null) {
         Kit var3 = null;
         Kit[] var4 = KitManager.getKits();
         int var5 = var4.length;

          for (Kit var7 : var4) {
              if (var7.getSlot() == var1.getSlot()) {
                  var3 = var7;
              }
          }

         if (var3 == null) {
            Console.debugWarn("MenuKitSelect.onClick - " + this.getPlayer().getName() + " has selected a NULL Kit");
         } else if (var3.isFree()) {
            if (ConfigManager.main.getBoolean("kit_permission") && !var2.hasPermissions("skywars.kit." + var3.getName().toLowerCase())) {
               var2.sendMessage(SkyWars.getMessage(Messages.PLAYER_NEEDPERMISSIONS_KIT));
            } else {
               var2.setKit(var3);
               var2.sendMessage(String.format(SkyWars.getMessage(Messages.PLAYER_SELECT_KIT), var3.getName().toLowerCase()));
               this.getPlayer().closeInventory();
            }
         } else if (!var2.hasKit(var3)) {
            if (ConfigManager.shop.getBoolean("shopingame")) {
               if (SkyWars.getPlugin().getConfig().getBoolean("kit_permission") && !var2.hasPermissions("skywars.kit." + var3.getName().toLowerCase())) {
                  var2.sendMessage(SkyWars.getMessage(Messages.PLAYER_NEEDPERMISSIONS_KIT));
               } else if (var3.isFree()) {
                  var2.setKit(var3);
                  var2.sendMessage(String.format(SkyWars.getMessage(Messages.PLAYER_PURCHASE_KIT), var3.getName().toLowerCase()));
               } else if (var2.getCoins() >= (double)var3.getPrice()) {
                  SkyEconomyManager.removeCoins(var2.getPlayer(), var3.getPrice());
                  var2.setKit(var3);
                  var2.sendMessage(String.format(SkyWars.getMessage(Messages.PLAYER_PURCHASE_KIT), var3.getName().toLowerCase()));
                  var2.addData("upload_data", true);
                  var2.addKit(var3);
                  this.getPlayer().closeInventory();
               } else {
                  var2.sendMessage(SkyWars.getMessage(Messages.PLAYER_NEEDMONEY_KIT));
               }
            }
         } else {
            var2.setKit(var3);
            var2.sendMessage(String.format(SkyWars.getMessage(Messages.PLAYER_SELECT_KIT), var3.getName().toLowerCase()));
            this.getPlayer().closeInventory();
         }
      }
   }

   public void update() {
      Kit[] var1 = KitManager.getKits();
      int var2 = var1.length;

       for (Kit var4 : var1) {
           ItemBuilder var5 = var4.getIcon().setHideFlags(true).clone();
           SkyPlayer var6 = SkyWars.getSkyPlayer(this.getPlayer());
           if (var6.hasKit(var4)) {
               var5.setTitle(String.format(SkyWars.getMessage(Messages.KIT_NAME_PURCHASED), var4.getName()));
           } else if (ConfigManager.main.getBoolean("menu.kits.unavailable.enabled") && !var4.isFree()) {
               ItemBuilder var7 = Utils.readItem(ConfigManager.main.getString("menu.kits.unavailable.item"));
               var5.setType(var7.getType());
               var5.setData(var7.getData());
               var5.setGlow(false);
           }

           if (!ConfigManager.shop.getBoolean("shopingame")) {
               var5.setLore(var4.getContents());
           }

           this.setItem(var4.getSlot(), var5);
       }

   }
}
