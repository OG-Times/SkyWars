package fun.ogtimes.skywars.spigot.events;

import fun.ogtimes.skywars.spigot.arena.Arena;
import fun.ogtimes.skywars.spigot.player.SkyPlayer;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
public class ArenaFinishEvent extends Event {
   @Getter
   private static final HandlerList handlerList = new HandlerList();
   private final Arena arena;
   private final SkyPlayer winner;

    public HandlerList getHandlers() {
      return handlerList;
   }

    public ArenaFinishEvent(Arena var1, SkyPlayer var2) {
      this.arena = var1;
      this.winner = var2;
   }
}
