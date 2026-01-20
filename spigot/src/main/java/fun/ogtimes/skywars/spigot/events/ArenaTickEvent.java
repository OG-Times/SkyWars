package fun.ogtimes.skywars.spigot.events;

import fun.ogtimes.skywars.spigot.arena.Arena;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
public class ArenaTickEvent extends Event {
   private static final HandlerList handlers = new HandlerList();
   private final Arena arena;

   public ArenaTickEvent(Arena var1) {
      this.arena = var1;
   }

    public HandlerList getHandlers() {
      return handlers;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }
}
