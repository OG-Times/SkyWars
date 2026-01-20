package fun.ogtimes.skywars.spigot.events;

import fun.ogtimes.skywars.spigot.arena.Arena;
import fun.ogtimes.skywars.spigot.events.enums.ArenaLeaveCause;
import fun.ogtimes.skywars.spigot.player.SkyPlayer;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
public class ArenaLeaveEvent extends Event {
   private static final HandlerList handlers = new HandlerList();
   private final SkyPlayer player;
   private final Arena game;
   private final ArenaLeaveCause cause;

   public ArenaLeaveEvent(SkyPlayer var1, Arena var2, ArenaLeaveCause var3) {
      this.player = var1;
      this.game = var2;
      this.cause = var3;
   }

    public HandlerList getHandlers() {
      return handlers;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }
}
