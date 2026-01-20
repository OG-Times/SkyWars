package ak.CookLoco.SkyWars.utils.leaderheads.stats;

import ak.CookLoco.SkyWars.SkyWars;
import ak.CookLoco.SkyWars.player.SkyPlayer;
import java.util.Arrays;
import me.robin.leaderheads.datacollectors.OnlineDataCollector;
import me.robin.leaderheads.objects.BoardType;
import org.bukkit.entity.Player;

public class BlocksBroken extends OnlineDataCollector {
   public BlocksBroken() {
      super("sw-blocks-break", "SkyWars", BoardType.DEFAULT, "SkyWars - Top Blocks Broken", "swBBroken", Arrays.asList(null, "&9{name}", "&6{amount}", null));
   }

   public Double getScore(Player var1) {
      SkyPlayer var2 = SkyWars.getSkyPlayer(var1);
      return var2 != null ? (double)var2.getBlocksBroken() : null;
   }
}
