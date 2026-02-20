package fun.ogtimes.skywars.spigot.utils;

import fun.ogtimes.skywars.spigot.SkyWars;
import java.util.HashMap;
import java.util.Iterator;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;

public class BoardAPI {
    public static void scoredSidebar(Player var0, String var67, HashMap<String, Integer> var2) {
        if (var67 == null) {
            var67 = "Unamed board";
        }
        if (var67.length() > 32) {
            var67 = var67.substring(0, 32);
        }
        String var4;
        label39:
        for (; var2.size() > 16; var2.remove(var4)) {
            var4 = (String) var2.keySet().toArray()[0];
            int var5 = var2.get(var4);
            Iterator<String> var6 = var2.keySet().iterator();
            while (true) {
                String var7;
                do {
                    if (!var6.hasNext()) continue label39;
                    var7 = var6.next();
                } while (var2.get(var7) >= var5 && (var2.get(var7) != var5 || var7.compareTo(var4) >= 0));
                var4 = var7;
                var5 = var2.get(var7);
            }
        }

        String finalTitle = var67;
        Bukkit.getScheduler().runTask(SkyWars.getPlugin(), () -> {
            if (var0 == null || !var0.isOnline()) return;

            if (var0.getScoreboard() == null ||
                    var0.getScoreboard() == Bukkit.getScoreboardManager().getMainScoreboard()) {
                var0.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
            }

            Objective obj = var0.getScoreboard().getObjective(DisplaySlot.SIDEBAR);
            if (obj == null) {
                obj = var0.getScoreboard().registerNewObjective(
                        finalTitle.length() > 16 ? finalTitle.substring(0, 15) : finalTitle,
                        "dummy"
                );
            }
            obj.setDisplayName(finalTitle);
            if (obj.getDisplaySlot() != DisplaySlot.SIDEBAR) {
                obj.setDisplaySlot(DisplaySlot.SIDEBAR);
            }

            final Objective finalObj = obj;
            Bukkit.getScheduler().runTaskAsynchronously(SkyWars.getPlugin(), () -> {
                Iterator<String> it = var2.keySet().iterator();
                while (true) {
                    String entry;
                    do {
                        if (!it.hasNext()) {
                            // Clean up removed entries
                            Iterator<String> cleanup = var0.getScoreboard().getEntries().iterator();
                            while (cleanup.hasNext()) {
                                String e = cleanup.next();
                                if (finalObj.getScore(e).isScoreSet() && !var2.containsKey(e)) {
                                    var0.getScoreboard().resetScores(e);
                                }
                            }
                            return;
                        }
                        entry = it.next();
                    } while (finalObj.getScore(entry).isScoreSet() &&
                             finalObj.getScore(entry).getScore() == var2.get(entry));

                    finalObj.getScore(entry).setScore(var2.get(entry));
                }
            });
        });
    }
}
