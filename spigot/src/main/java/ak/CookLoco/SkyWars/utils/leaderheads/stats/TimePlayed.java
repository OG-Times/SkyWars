package ak.CookLoco.SkyWars.utils.leaderheads.stats;

import ak.CookLoco.SkyWars.SkyWars;
import ak.CookLoco.SkyWars.player.SkyPlayer;
import java.util.Arrays;
import me.robin.leaderheads.datacollectors.OnlineDataCollector;
import me.robin.leaderheads.objects.BoardType;
import org.bukkit.entity.Player;

public class TimePlayed extends OnlineDataCollector {
   public TimePlayed() {
      super("sw-time-played", "SkyWars", BoardType.TIME, "SkyWars - Top Time Played", "swTPlayed", Arrays.asList(null, "&9{name}", "&6{amount}", null));
   }

   public Double getScore(Player var1) {
      SkyPlayer var2 = SkyWars.getSkyPlayer(var1);
      return var2 != null ? (double)var2.getTimePlayed() : null;
   }
}
