package fun.ogtimes.skywars.spigot.menus;

import fun.ogtimes.skywars.spigot.utils.ItemBuilder;
import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public abstract class Menu {
    @Getter
    private final String menuId;
    private Inventory inv;
    private final String player;
    @Setter
    @Getter
    private String back;

    public Menu(Player player, String id, String title, int rows) {
        this.player = player.getName();
        this.menuId = id;
        this.inv = Bukkit.createInventory(null, rows * 9, ChatColor.translateAlternateColorCodes('&', title));
        this.setBack("none");
        Map<String, Menu> var5 = MenuListener.getPlayerMenus(player);
        var5.put(id, this);
        MenuListener.menus.put(player.getName(), var5);
    }

    public Menu(Player player, String id, String title, int rows, String back) {
        this.player = player.getName();
        this.menuId = id;
        this.inv = Bukkit.createInventory(null, rows * 9, ChatColor.translateAlternateColorCodes('&', title));
        this.setBack(back);
        Map<String, Menu> var6 = MenuListener.getPlayerMenus(player);
        var6.put(id, this);
        MenuListener.menus.put(player.getName(), var6);
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
