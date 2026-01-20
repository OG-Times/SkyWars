package fun.ogtimes.skywars.spigot.events;

import fun.ogtimes.skywars.spigot.arena.Arena;
import fun.ogtimes.skywars.spigot.events.enums.ArenaLeaveCause;
import fun.ogtimes.skywars.spigot.player.SkyPlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ArenaLeaveEvent extends Event {
   private static final HandlerList handlers = new HandlerList();
   private SkyPlayer player;
   private Arena game;
   private ArenaLeaveCause cause;

   public ArenaLeaveEvent(SkyPlayer var1, Arena var2, ArenaLeaveCause var3) {
      this.player = var1;
      this.game = var2;
      this.cause = var3;
   }

   public SkyPlayer getPlayer() {
      return this.player;
   }

   public Arena getGame() {
      return this.game;
   }

   public ArenaLeaveCause getCause() {
      return this.cause;
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }
}
