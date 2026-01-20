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
        for(; var2.size() > 16; var2.remove(var4)) {
            var4 = (String)var2.keySet().toArray()[0];
            int var5 = var2.get(var4);
            Iterator<String> var6 = var2.keySet().iterator();

            while(true) {
                String var7;
                do {
                    if (!var6.hasNext()) {
                        continue label39;
                    }

                    var7 = (String)var6.next();
                } while((Integer)var2.get(var7) >= var5 && ((Integer)var2.get(var7) != var5 || var7.compareTo(var4) >= 0));

                var4 = var7;
                var5 = (Integer)var2.get(var7);
            }
        }

        String finalVar6 = var67;
        Bukkit.getScheduler().runTask(SkyWars.getPlugin(), () -> {
            if (var0 != null && var0.isOnline()) {
                if (Bukkit.getScoreboardManager().getMainScoreboard() != null && Bukkit.getScoreboardManager().getMainScoreboard() == var0.getScoreboard()) {
                    var0.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
                }

                if (var0.getScoreboard() == null) {
                    var0.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
                }

                Bukkit.getScheduler().runTaskAsynchronously(SkyWars.getPlugin(), () -> {
                    Objective var3 = var0.getScoreboard().getObjective(DisplaySlot.SIDEBAR);
                    if (var3 == null) {
                        var3 = var0.getScoreboard().registerNewObjective(finalVar6.length() > 16 ? finalVar6.substring(0, 15) : finalVar6, "dummy");
                    }

                    var3.setDisplayName(finalVar6);
                    if (var3.getDisplaySlot() == null || var3.getDisplaySlot() != DisplaySlot.SIDEBAR) {
                        var3.setDisplaySlot(DisplaySlot.SIDEBAR);
                    }

                    Iterator<String> var728572 = var2.keySet().iterator();

                    while(true) {
                        String var5;
                        do {
                            if (!var728572.hasNext()) {
                                var728572 = var0.getScoreboard().getEntries().iterator();

                                while(var728572.hasNext()) {
                                    var5 = (String)var728572.next();
                                    if (var3.getScore(var5).isScoreSet() && !var2.containsKey(var5)) {
                                        var0.getScoreboard().resetScores(var5);
                                    }
                                }

                                return;
                            }

                            var5 = (String)var728572.next();
                        } while(var3.getScore(var5).isScoreSet() && var3.getScore(var5).getScore() == (Integer)var2.get(var5));

                        var3.getScore(var5).setScore((Integer)var2.get(var5));
                    }
                });
            }
        });
    }
}
