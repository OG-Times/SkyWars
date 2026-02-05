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

@Getter @Setter
public abstract class Menu {
    private final String menuId;
    private Inventory inv;
    private final String player;
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

    public Menu addItem(ItemStack item) {
        this.inv.addItem(item);
        return this;
    }

    public Menu addItem(ItemBuilder item) {
        return this.addItem(item.build());
    }

    public Menu setItem(int slot, ItemBuilder item) {
        this.inv.setItem(slot, item.build());
        return this;
    }

    public Menu setItem(int x, int y, ItemBuilder item) {
        this.inv.setItem((x - 1) * 9 + (y - 1), item.build());
        return this;
    }

    public Menu setItem(int x, int y, ItemStack item) {
        this.inv.setItem(x * 9 + y, item);
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

        for(int i = 1; i < 10; ++i) {
            this.setItem(var1, i, var2);
        }

    }

    public abstract void onOpen(InventoryOpenEvent event);

    public abstract void onClose(InventoryCloseEvent event);

    public abstract void onClick(InventoryClickEvent event);

    public abstract void update();
}
