package fun.ogtimes.skywars.spigot.server;

import fun.ogtimes.skywars.spigot.SkyWars;
import fun.ogtimes.skywars.spigot.arena.ArenaState;
import fun.ogtimes.skywars.spigot.database.DatabaseHandler;
import fun.ogtimes.skywars.spigot.events.SkySignUpdateEvent;
import fun.ogtimes.skywars.spigot.events.enums.SkySignUpdateCause;
import fun.ogtimes.skywars.spigot.utils.Game;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

import org.bukkit.Bukkit;

public class Server extends Game {
    public Server(String name) {
        super(name, "", 0, true, ArenaState.WAITING);
        this.getData(false);
    }

    public void getData(boolean var1) {
        PreparedStatement statement = null;
        ResultSet result = null;

        try {
            Connection connection = DatabaseHandler.getDS().getConnection();
            Throwable ex = null;

            try {
                statement = connection.prepareStatement(String.format("SELECT * FROM %s WHERE bungeeid= ?", DatabaseHandler.getDS().TABLE_SERVER));
                statement.setString(1, this.name);
                statement.execute();
                result = statement.getResultSet();
                if (result.next()) {
                    List<SkySignUpdateCause> var6 = new ArrayList<>();
                    String bungeeId = result.getString("bungeeid");
                    int alivePlayers = result.getInt("players");
                    int maxPlayers = result.getInt("max_players");
                    int loading = result.getInt("loading");
                    int var11 = this.loading ? 1 : 0;
                    String state = result.getString("state");
                    String displayName = result.getString("map");

                    if (this.alivePlayers != alivePlayers || this.maxPlayers != maxPlayers) {
                        var6.add(SkySignUpdateCause.PLAYERS);
                    }

                    if (var11 != loading) {
                        var6.add(SkySignUpdateCause.LOADING);
                    }

                    if (state != null) {
                        if (!this.state.name().equalsIgnoreCase(state.trim())) {
                            var6.add(SkySignUpdateCause.STATE);
                        }
                    }

                    if (displayName != null) {
                        if (!Objects.equals(this.displayName, displayName)) {
                            var6.add(SkySignUpdateCause.MAP);
                        }
                    } else {
                        if (this.displayName != null && !this.displayName.isEmpty()) {
                            var6.add(SkySignUpdateCause.MAP);
                        }
                    }

                    SkySignUpdateCause signUpdateCause = null;
                    if (var6.size() == 1) {
                        signUpdateCause = var6.get(0);
                    }

                    if (var6.size() >= 2) {
                        signUpdateCause = SkySignUpdateCause.ALL;
                    }

                    this.alivePlayers = alivePlayers;
                    this.maxPlayers = maxPlayers;
                    this.loading = loading == 1;
                    if (state != null) {
                        try {
                            this.state = ArenaState.valueOf(state.trim().toUpperCase());
                        } catch (IllegalArgumentException iae) {
                            SkyWars.getPlugin().getLogger().warning("Invalid ArenaState in DB for server " + this.name + ": " + state);
                        }
                    }
                    this.displayName = (displayName == null) ? this.displayName : displayName;
                    if (!var6.isEmpty() && var1) {
                        SkySignUpdateCause finalVar1 = signUpdateCause;
                        Bukkit.getScheduler().runTask(SkyWars.getPlugin(), () -> Bukkit.getServer().getPluginManager().callEvent(new SkySignUpdateEvent(bungeeId, finalVar1)));
                    }

                    statement.close();
                }
            } catch (Throwable throwable) {
                ex = throwable;
                throw throwable;
            } finally {
                if (connection != null) {
                    if (ex != null) {
                        try {
                            connection.close();
                        } catch (Throwable var31) {
                            ex.addSuppressed(var31);
                        }
                    } else {
                        connection.close();
                    }
                }

            }
        } catch (SQLException ex) {
            SkyWars.getPlugin().getLogger().log(Level.SEVERE, "SQL error while getting server data for " + this.name, ex);
        } finally {
            DatabaseHandler.getDS().close(result);
            DatabaseHandler.getDS().close(statement);
        }

    }
}
