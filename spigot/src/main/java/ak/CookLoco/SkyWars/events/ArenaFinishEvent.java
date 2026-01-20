package ak.CookLoco.SkyWars.events;

import ak.CookLoco.SkyWars.arena.Arena;
import ak.CookLoco.SkyWars.player.SkyPlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ArenaFinishEvent extends Event {
   private static final HandlerList handlerList = new HandlerList();
   private final Arena arena;
   private final SkyPlayer winner;

   public static HandlerList getHandlerList() {
      return handlerList;
   }

   public HandlerList getHandlers() {
      return handlerList;
   }

   public Arena getArena() {
      return this.arena;
   }

   public SkyPlayer getWinner() {
      return this.winner;
   }

   public ArenaFinishEvent(Arena var1, SkyPlayer var2) {
      this.arena = var1;
      this.winner = var2;
   }
}
