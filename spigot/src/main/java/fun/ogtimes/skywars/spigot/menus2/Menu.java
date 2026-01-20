package fun.ogtimes.skywars.spigot.menus2;

import fun.ogtimes.skywars.spigot.utils.ItemBuilder;
import java.util.HashMap;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public abstract class Menu {
   @Getter
   private final String menuId;
   private Inventory inv;
   private final String player;
   @Setter
   @Getter
   private String back;

   public Menu(Player var1, String var2, String var3, int var4) {
      this.player = var1.getName();
      this.menuId = var2;
      this.inv = Bukkit.createInventory(null, var4 * 9, ChatColor.translateAlternateColorCodes('&', var3));
      this.setBack("none");
      HashMap var5 = MenuListener.getPlayerMenus(var1);
      var5.put(var2, this);
      MenuListener.menus.put(var1.getName(), var5);
   }

   public Menu(Player var1, String var2, String var3, int var4, String var5) {
      this.player = var1.getName();
      this.menuId = var2;
      this.inv = Bukkit.createInventory(null, var4 * 9, ChatColor.translateAlternateColorCodes('&', var3));
      this.setBack(var5);
      HashMap var6 = MenuListener.getPlayerMenus(var1);
      var6.put(var2, this);
      MenuListener.menus.put(var1.getName(), var6);
   }

   public Menu addItem(ItemStack var1) {
      this.inv.addItem(var1);
      return this;
   }

   public Menu addItem(ItemBuilder var1) {
      return this.addItem(var1.build());
   }

   public Menu setItem(int var1, ItemBuilder var2) {
      this.inv.setItem(var1, var2.build());
      return this;
   }

   public Menu setItem(int var1, int var2, ItemBuilder var3) {
      this.inv.setItem((var1 - 1) * 9 + (var2 - 1), var3.build());
      return this;
   }

   public Menu setItem(int var1, int var2, ItemStack var3) {
      this.inv.setItem(var1 * 9 + var2, var3);
      return this;
   }

   public Inventory getInventory() {
      return this.inv;
   }

   public void newInventoryName(String var1) {
      this.inv = Bukkit.createInventory(null, this.inv.getSize(), var1);
   }

    public Player getPlayer() {
      return Bukkit.getPlayer(this.player);
   }

    public void addFullLine(int var1, ItemBuilder var2) {
      var2.setTitle(" &r");

      for(int var3 = 1; var3 < 10; ++var3) {
         this.setItem(var1, var3, var2);
      }

   }

   public abstract void onOpen(InventoryOpenEvent var1);

   public abstract void onClose(InventoryCloseEvent var1);

   public abstract void onClick(InventoryClickEvent var1);

   public abstract void update();
}
