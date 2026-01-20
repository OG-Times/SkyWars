package fun.ogtimes.skywars.spigot.events;

import fun.ogtimes.skywars.spigot.events.enums.SkySignUpdateCause;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
public class SkySignUpdateEvent extends Event {
   private static final HandlerList handlers = new HandlerList();
   private final String arena;
   private final SkySignUpdateCause cause;

   public SkySignUpdateEvent(String var1, SkySignUpdateCause var2) {
      this.arena = var1;
      this.cause = var2;
   }

    public HandlerList getHandlers() {
      return handlers;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }
}
