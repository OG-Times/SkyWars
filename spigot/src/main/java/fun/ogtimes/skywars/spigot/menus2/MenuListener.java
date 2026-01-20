package fun.ogtimes.skywars.spigot.menus2;

import java.util.HashMap;
import java.util.Iterator;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class MenuListener implements Listener {
    public static final HashMap<String, HashMap<String, Menu>> menus = new HashMap<>();

    public static HashMap<String, Menu> getPlayerMenus(Player var0) {
        return menus.containsKey(var0.getName()) ? menus.get(var0.getName()) : new HashMap();
    }

    public static Menu getPlayerMenu(Player var0, String var1) {
        return getPlayerMenus(var0).getOrDefault(var1, null);
    }

    @EventHandler
    public void onPlayerLeaveInvRemove(PlayerQuitEvent var1) {
        menus.remove(var1.getPlayer().getName());
    }

    @EventHandler
    public void onPlayerKickInvRemove(PlayerKickEvent var1) {
        menus.remove(var1.getPlayer().getName());
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent var1) {

        for (Menu var3 : getPlayerMenus((Player) var1.getPlayer()).values()) {
            if (var1.getInventory().getTitle().equals(var3.getInventory().getTitle())) {
                var3.onOpen(var1);
            }
        }

    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent var1) {

        for (Menu var3 : getPlayerMenus((Player) var1.getPlayer()).values()) {
            if (var1.getInventory().getTitle().equals(var3.getInventory().getTitle())) {
                var3.onClose(var1);
            }
        }

    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent var1) {

        for (Menu var3 : getPlayerMenus((Player) var1.getWhoClicked()).values()) {
            if (var1.getInventory().getTitle().equals(var3.getInventory().getTitle()) && var1.getCurrentItem() != null) {
                var1.setCancelled(true);
                var3.onClick(var1);
            }
        }

    }
}
