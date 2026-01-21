package fun.ogtimes.skywars.spigot.events;

import fun.ogtimes.skywars.spigot.arena.Arena;
import fun.ogtimes.skywars.spigot.player.SkyPlayer;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.PlayerDeathEvent;

@Getter
public class SkyPlayerDeathEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final SkyPlayer player;
    private final SkyPlayer killer;
    private final Arena game;
    private final PlayerDeathEvent originalEvent;

    public SkyPlayerDeathEvent(SkyPlayer player, SkyPlayer killer, Arena game, PlayerDeathEvent originalEvent) {
        this.player = player;
        this.killer = killer;
        this.game = game;
        this.originalEvent = originalEvent;
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
