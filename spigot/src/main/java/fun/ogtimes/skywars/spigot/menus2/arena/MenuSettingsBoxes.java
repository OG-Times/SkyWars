package fun.ogtimes.skywars.spigot.menus2.arena;

import fun.ogtimes.skywars.spigot.SkyWars;
import fun.ogtimes.skywars.spigot.arena.ArenaBox;
import fun.ogtimes.skywars.spigot.box.Box;
import fun.ogtimes.skywars.spigot.box.BoxManager;
import fun.ogtimes.skywars.spigot.menus2.Menu;
import fun.ogtimes.skywars.spigot.player.SkyPlayer;
import fun.ogtimes.skywars.spigot.utils.Messages;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

public class MenuSettingsBoxes extends Menu {
   public MenuSettingsBoxes(Player var1) {
      super(var1, "settingsBoxes", SkyWars.getMessage(Messages.SETTINGS_MENU_BOXES_TITLE), SkyWars.boxes.getInt("menu_rows"));
   }

   public void onOpen(InventoryOpenEvent var1) {
      this.update();
   }

   public void onClose(InventoryCloseEvent var1) {
   }

   public void onClick(InventoryClickEvent var1) {
      SkyPlayer var2 = SkyWars.getSkyPlayer(this.getPlayer());
      if (var2 != null) {
         Box[] var3 = BoxManager.getBoxes();
         int var4 = var3.length;

          for (Box var6 : var3) {
              if (var1.getCurrentItem().isSimilar(var6.getItemBuilder().build())) {
                  if (!var6.getSection().equals(SkyWars.boxes.getString("default")) && !var2.hasPermission("skywars.settings.colour." + var6.getSection())) {
                      var2.sendMessage(SkyWars.getMessage(Messages.PLAYER_NEEDPERMISSIONS_SETTINGS_COLOUR));
                      return;
                  }

                  ArenaBox var7 = var2.getBox();
                  var7.setBox(var6.getItem(), var6.getData());
                  var2.addData("upload_data", true);
                  var2.setBoxSection(var6.getSection(), true);
                  if (var6.getSection().equals(SkyWars.boxes.getString("default"))) {
                      var2.sendMessage(SkyWars.getMessage(Messages.PLAYER_SELECT_COLOUR_DEFAULT));
                  } else {
                      var2.sendMessage(String.format(SkyWars.getMessage(Messages.PLAYER_SELECT_COLOUR), var6.getName()));
                  }
              }
          }

      }
   }

   public void update() {
      Box[] var1 = BoxManager.getBoxes();
      int var2 = var1.length;

       for (Box var4 : var1) {
           this.setItem(var4.getSlot() - 1, var4.getItemBuilder());
       }

   }
}
