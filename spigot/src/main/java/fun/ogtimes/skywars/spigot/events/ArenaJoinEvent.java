package fun.ogtimes.skywars.spigot.events;

import fun.ogtimes.skywars.spigot.arena.Arena;
import fun.ogtimes.skywars.spigot.events.enums.ArenaJoinCause;
import fun.ogtimes.skywars.spigot.player.SkyPlayer;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
public class ArenaJoinEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final SkyPlayer player;
    private final Arena game;
    private final ArenaJoinCause cause;

    public ArenaJoinEvent(SkyPlayer player, Arena arena, ArenaJoinCause cause) {
        this.player = player;
        this.game = arena;
        this.cause = cause;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
