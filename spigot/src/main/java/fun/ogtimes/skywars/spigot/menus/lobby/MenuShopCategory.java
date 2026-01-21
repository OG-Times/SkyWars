package fun.ogtimes.skywars.spigot.menus.lobby;

import fun.ogtimes.skywars.spigot.SkyWars;
import fun.ogtimes.skywars.spigot.config.ConfigManager;
import fun.ogtimes.skywars.spigot.kit.Kit;
import fun.ogtimes.skywars.spigot.kit.KitManager;
import fun.ogtimes.skywars.spigot.menus.Menu;
import fun.ogtimes.skywars.spigot.player.SkyPlayer;
import fun.ogtimes.skywars.spigot.utils.ItemBuilder;
import fun.ogtimes.skywars.spigot.utils.Messages;
import fun.ogtimes.skywars.spigot.utils.economy.SkyEconomyManager;
import java.util.Iterator;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

public class MenuShopCategory extends Menu {
   private final String section;

   public MenuShopCategory(Player player, String id) {
      super(player, "shopCategory-" + id, ConfigManager.shop.getString("submenus." + id + ".sub_name"), ConfigManager.shop.getInt("shop_size"));
      this.section = id;
   }

   public void onOpen(InventoryOpenEvent event) {
      this.update();
   }

   public void onClose(InventoryCloseEvent event) {
   }

   public void onClick(InventoryClickEvent event) {
      int slot = event.getSlot();
      if (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR) {
         SkyPlayer skyPlayer = SkyWars.getSkyPlayer(this.getPlayer());
         if (!MenuShop.executeSubMenuButtons(slot, skyPlayer)) {

             for (String var5 : ConfigManager.shop.getConfigurationSection("submenus." + this.section + ".content").getKeys(false)) {
                 if (slot == Integer.parseInt(var5) - 1) {
                     String[] var6 = ConfigManager.shop.getString("submenus." + this.section + ".content." + var5 + ".item").split(",");
                     Kit var7 = KitManager.getKit(var6[1]);
                     if (var7 == null) {
                         return;
                     }

                     if (!skyPlayer.hasKit(var7)) {
                         if (SkyWars.getPlugin().getConfig().getBoolean("kit_permission") && !skyPlayer.hasPermission("skywars.kit." + var7.getName().toLowerCase())) {
                             skyPlayer.sendMessage(SkyWars.getMessage(Messages.PLAYER_NEEDPERMISSIONS_KIT));
                             return;
                         }

                         if (var7.isFree()) {
                             skyPlayer.sendMessage(String.format(SkyWars.getMessage(Messages.PLAYER_PURCHASE_KIT), var7.getName().toLowerCase()));
                             return;
                         }

                         if (skyPlayer.getCoins() >= (double) var7.getPrice()) {
                             SkyEconomyManager.removeCoins(skyPlayer.getPlayer(), var7.getPrice());
                             skyPlayer.sendMessage(String.format(SkyWars.getMessage(Messages.PLAYER_PURCHASE_KIT), var7.getName().toLowerCase()));
                             skyPlayer.addData("upload_data", true);
                             skyPlayer.addKit(var7);
                             skyPlayer.getPlayer().closeInventory();
                             return;
                         }

                         skyPlayer.sendMessage(SkyWars.getMessage(Messages.PLAYER_NEEDMONEY_KIT));
                         return;
                     }

                     skyPlayer.sendMessage(String.format(SkyWars.getMessage(Messages.PLAYER_ALREADY_KIT), var7.getName().toLowerCase()));
                 }
             }

         }
      }
   }

   public void update() {
      SkyPlayer var1 = SkyWars.getSkyPlayer(this.getPlayer());
      Iterator var2 = ConfigManager.shop.getConfigurationSection("submenus." + this.section + ".content").getKeys(false).iterator();

      String var3;
      int var4;
      while(var2.hasNext()) {
         var3 = (String)var2.next();
         var4 = Integer.parseInt(var3) - 1;
         String[] var5 = ConfigManager.shop.getString("submenus." + this.section + ".content." + var3 + ".item").split(",");
         if (var5[0].equalsIgnoreCase("KIT")) {
            Kit var6 = KitManager.getKit(var5[1]);
            if (var6 != null) {
               ItemBuilder var7 = var6.getIcon().setHideFlags(true);
               if (var1.hasKit(var6)) {
                  var7.setTitle(String.format(SkyWars.getMessage(Messages.KIT_NAME_PURCHASED), var6.getName()));
               }

               this.setItem(var4, var7);
            }
         }
      }

      var2 = ConfigManager.shop.getConfigurationSection("all").getKeys(false).iterator();

      while(var2.hasNext()) {
         var3 = (String)var2.next();
         var4 = Integer.parseInt(var3) - 1;
         this.setItem(var4, MenuShop.getItemRead("all." + var3, ConfigManager.shop, true, var1));
      }

      var2 = ConfigManager.shop.getConfigurationSection("allsubmenus").getKeys(false).iterator();

      while(var2.hasNext()) {
         var3 = (String)var2.next();
         var4 = Integer.parseInt(var3) - 1;
         this.setItem(var4, MenuShop.getItemRead("allsubmenus." + var3, ConfigManager.shop, true, null));
      }

   }
}
