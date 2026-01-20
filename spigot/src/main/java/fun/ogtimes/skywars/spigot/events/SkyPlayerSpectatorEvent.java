package fun.ogtimes.skywars.spigot.events;

import fun.ogtimes.skywars.spigot.arena.Arena;
import fun.ogtimes.skywars.spigot.events.enums.SpectatorReason;
import fun.ogtimes.skywars.spigot.player.SkyPlayer;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SkyPlayerSpectatorEvent extends Event {
   private static final HandlerList handlers = new HandlerList();
   @Getter
   private final SkyPlayer player;
   @Getter
   private final Arena game;
   private final boolean spectate;
   @Getter
   private final SpectatorReason reason;

   public SkyPlayerSpectatorEvent(SkyPlayer var1, Arena var2, boolean var3, SpectatorReason var4) {
      this.player = var1;
      this.game = var2;
      this.spectate = var3;
      this.reason = var4;
   }

    public boolean getSpectate() {
      return this.spectate;
   }

    public boolean isDeathReason() {
      return this.reason == SpectatorReason.DEATH;
   }

   public boolean isJoinReason() {
      return this.reason == SpectatorReason.JOIN;
   }

   public boolean isLeaveReason() {
      return this.reason == SpectatorReason.LEAVE;
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }
}
