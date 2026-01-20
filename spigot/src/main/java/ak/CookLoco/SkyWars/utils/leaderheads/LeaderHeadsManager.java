package ak.CookLoco.SkyWars.utils.leaderheads;

import ak.CookLoco.SkyWars.SkyWars;
import ak.CookLoco.SkyWars.utils.leaderheads.stats.ArrowHit;
import ak.CookLoco.SkyWars.utils.leaderheads.stats.ArrowShot;
import ak.CookLoco.SkyWars.utils.leaderheads.stats.BlocksBroken;
import ak.CookLoco.SkyWars.utils.leaderheads.stats.BlocksPlaced;
import ak.CookLoco.SkyWars.utils.leaderheads.stats.Deaths;
import ak.CookLoco.SkyWars.utils.leaderheads.stats.DistanceWalked;
import ak.CookLoco.SkyWars.utils.leaderheads.stats.Kills;
import ak.CookLoco.SkyWars.utils.leaderheads.stats.Played;
import ak.CookLoco.SkyWars.utils.leaderheads.stats.TimePlayed;
import ak.CookLoco.SkyWars.utils.leaderheads.stats.Wins;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class LeaderHeadsManager {
   public static void load() {
      Plugin var0 = Bukkit.getPluginManager().getPlugin("LeaderHeads");
      if (var0 != null) {
         new Wins();
         new Kills();
         new Deaths();
         new Played();
         new ArrowShot();
         new ArrowHit();
         new BlocksBroken();
         new BlocksPlaced();
         new TimePlayed();
         new DistanceWalked();
         SkyWars.console(SkyWars.prefix + "&aLeaderHeads hook enabled");
      }

   }
}
