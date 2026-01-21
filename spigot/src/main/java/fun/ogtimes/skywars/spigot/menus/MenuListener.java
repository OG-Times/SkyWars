package fun.ogtimes.skywars.spigot.menus;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class MenuListener implements Listener {
    public static final Map<String, Map<String, Menu>> menus = new HashMap<>();

    public static Map<String, Menu> getPlayerMenus(Player player) {
        return menus.containsKey(player.getName()) ? menus.get(player.getName()) : new HashMap<>();
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
    public void onInventoryOpen(InventoryOpenEvent event) {

        for (Menu menu : getPlayerMenus((Player) event.getPlayer()).values()) {
            if (event.getInventory().getTitle().equals(menu.getInventory().getTitle())) {
                menu.onOpen(event);
            }
        }

    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {

        for (Menu menu : getPlayerMenus((Player) event.getPlayer()).values()) {
            if (event.getInventory().getTitle().equals(menu.getInventory().getTitle())) {
                menu.onClose(event);
            }
        }

    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {

        for (Menu menu : getPlayerMenus((Player) event.getWhoClicked()).values()) {
            if (event.getInventory().getTitle().equals(menu.getInventory().getTitle()) && event.getCurrentItem() != null) {
                event.setCancelled(true);
                menu.onClick(event);
            }
        }

    }
}
