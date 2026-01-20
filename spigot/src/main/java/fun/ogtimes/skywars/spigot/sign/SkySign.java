package fun.ogtimes.skywars.spigot.sign;

import fun.ogtimes.skywars.spigot.SkyWars;
import fun.ogtimes.skywars.spigot.arena.ArenaManager;
import fun.ogtimes.skywars.spigot.server.ServerManager;
import fun.ogtimes.skywars.spigot.utils.Game;
import fun.ogtimes.skywars.spigot.utils.LocationUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

public class SkySign {
   private final String location;
   @Setter
   @Getter
   private boolean rotation;
   @Setter
   @Getter
   private boolean updating;
   @Getter
   private Game game;

   public SkySign(String var1) {
      this.location = var1;
      this.rotation = false;
   }

   public Sign getSign() {
      Location var1 = LocationUtil.getLocation(this.location);
      if (var1 == null) {
         SkyWars.logError("Trying to use null location for ArenaSign: " + this.location);
         return null;
      } else if (var1.getWorld() == null) {
         SkyWars.logError("Trying to use null world location for ArenaSign: " + this.location);
         return null;
      } else {
         Block var2 = var1.getWorld().getBlockAt(var1);
         return var2 == null || var2.getType() != Material.WALL_SIGN && var2.getType() != Material.SIGN_POST ? null : (Sign)var2.getState();
      }
   }

   public Location getLocation() {
      return LocationUtil.getLocation(this.location);
   }

    public void setGame(String var1) {
      if (SkyWars.isMultiArenaMode()) {
         this.game = ArenaManager.getGame(var1);
      }

      if (SkyWars.isLobbyMode()) {
         this.game = ServerManager.getServer(var1);
      }

   }

}
