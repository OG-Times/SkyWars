package fun.ogtimes.skywars.spigot.utils.sky;

import fun.ogtimes.skywars.spigot.SkyWars;
import fun.ogtimes.skywars.spigot.config.ConfigManager;
import fun.ogtimes.skywars.spigot.player.SkyPlayer;
import java.util.HashMap;
import java.util.Iterator;

import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI;
import me.filoghost.holographicdisplays.api.hologram.Hologram;
import me.filoghost.holographicdisplays.api.hologram.VisibilitySettings;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class SkyHologram {
    public static HashMap<SkyPlayer, Hologram> holos = new HashMap();

    public static void createHologram(SkyPlayer var0) {
        if (SkyWars.holo && !SkyWars.getHoloLocations().isEmpty()) {
            Player var1 = var0.getPlayer();
            if (var1 == null) {
                SkyWars.logError("Hologram can't be created due to a NULL player: " + var0.getName());
            } else {
                Iterator var2 = SkyWars.getHoloLocations().iterator();

                while(true) {
                    while(var2.hasNext()) {
                        Location location = (Location)var2.next();
                        if (location == null) {
                            SkyWars.logError("Hologram can't be created for " + var1.getName() + " due to wrong location in scoreboard.yml");
                        } else if (location.getWorld() == null) {
                            SkyWars.logError("Hologram can't be created for " + var1.getName() + " due to wrong world location in scoreboard.yml");
                        } else {
                            Hologram hologram = HolographicDisplaysAPI.get(SkyWars.getPlugin()).createHologram(location);
                            holos.put(var0, hologram);

                            for (String var6 : ConfigManager.score.getStringList("hologram.lines")) {
                                hologram.getLines().appendText(ChatColor.translateAlternateColorCodes('&', SkyWars.variableManager.replaceText(var0, var6)));
                            }

                            VisibilitySettings visibility = hologram.getVisibilitySettings();
                            visibility.setIndividualVisibility(var1, VisibilitySettings.Visibility.VISIBLE);
                            visibility.setGlobalVisibility(VisibilitySettings.Visibility.HIDDEN);
                        }
                    }

                    return;
                }
            }
        }
    }

    public static void removeHologram(SkyPlayer var0) {
        if (SkyWars.holo) {

            for (Hologram hologram : HolographicDisplaysAPI.get(SkyWars.getPlugin()).getHolograms()) {
                if (hologram != null) {
                    VisibilitySettings var3 = hologram.getVisibilitySettings();
                    Player player = var0.getPlayer();
                    if (player == null) {
                        SkyWars.logError("Hologram can't be removed due to a NULL player: " + var0.getName());
                    } else if (var3.isVisibleTo(player)) {
                        hologram.delete();
                        holos.remove(var0);
                    }
                }
            }
        }

    }

    public static void reloadHolograms() {

        for (Player var1 : Bukkit.getOnlinePlayers()) {
            SkyPlayer var2 = SkyWars.getSkyPlayer(var1);
            if (var2 != null) {
                removeHologram(var2);
                createHologram(var2);
            }
        }

    }
}
