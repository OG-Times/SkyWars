package fun.ogtimes.skywars.spigot.events;

import fun.ogtimes.skywars.spigot.arena.Arena;
import fun.ogtimes.skywars.spigot.player.SkyPlayer;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.PlayerDeathEvent;

public class SkyPlayerDeathEvent extends Event {
   private static final HandlerList handlers = new HandlerList();
   @Getter
   private final SkyPlayer player;
   @Getter
   private final SkyPlayer killer;
   @Getter
   private final Arena game;
   private final PlayerDeathEvent originalEvent;

   public SkyPlayerDeathEvent(SkyPlayer var1, SkyPlayer var2, Arena var3, PlayerDeathEvent var4) {
      this.player = var1;
      this.killer = var2;
      this.game = var3;
      this.originalEvent = var4;
   }

    public PlayerDeathEvent getDeathEvent() {
      return this.originalEvent;
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }
}
