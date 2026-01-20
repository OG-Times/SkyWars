package fun.ogtimes.skywars.spigot.utils;

import fun.ogtimes.skywars.spigot.arena.ArenaState;
import fun.ogtimes.skywars.spigot.utils.sky.SkyData;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Game extends SkyData {
   protected String name;
   protected String displayName;
   protected int alivePlayers;
   protected int maxPlayers;
   protected boolean loading;
   protected ArenaState state;

   public Game(String var1, String var2, int var3, boolean var4, ArenaState var5) {
      this.name = var1;
      this.displayName = var2;
      this.maxPlayers = var3;
      this.loading = var4;
      this.state = var5;
      if (var2 == null) {
         this.displayName = var1;
      }

   }

    public boolean isDisabled() {
      return false;
   }

   public boolean isFull() {
      return this.alivePlayers >= this.maxPlayers;
   }
}
