package fun.ogtimes.skywars.spigot.utils;

import fun.ogtimes.skywars.spigot.arena.ArenaState;
import fun.ogtimes.skywars.spigot.utils.sky.SkyData;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Game extends SkyData {
    protected String name;
    protected String displayName;
    protected int alivePlayers;
    protected int maxPlayers;
    protected boolean loading;
    protected ArenaState state;

    public Game(String name, String displayName, int maxPlayers, boolean loading, ArenaState state) {
        this.name = name;
        this.displayName = displayName;
        this.maxPlayers = maxPlayers;
        this.loading = loading;
        this.state = state;

        if (displayName == null) {
            this.displayName = name;
        }
    }

    public boolean isDisabled() {
        return false;
    }

    public boolean isFull() {
        return this.alivePlayers >= this.maxPlayers;
    }
}
