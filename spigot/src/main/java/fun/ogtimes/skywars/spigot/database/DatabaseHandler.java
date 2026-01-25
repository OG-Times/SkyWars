package fun.ogtimes.skywars.spigot.database;

import fun.ogtimes.skywars.spigot.SkyWars;
import fun.ogtimes.skywars.spigot.database.types.MySQL;
import fun.ogtimes.skywars.spigot.database.types.SQLite;
import fun.ogtimes.skywars.spigot.config.ConfigManager;

import java.sql.SQLException;

public class DatabaseHandler {
    private static DataSource manager;

    public DatabaseHandler() throws SQLException, ClassNotFoundException {
        String type = "sqlite";
        try {
            if (ConfigManager.database != null && ConfigManager.database.isSet("type")) {
                type = ConfigManager.database.getString("type", "SQLite").toLowerCase();
            } else if (ConfigManager.main != null && ConfigManager.main.isSet("data.type")) {
                type = ConfigManager.main.getString("data.type", "SQLite").toLowerCase();
            }
        } catch (Exception ignored) {
        }
        byte var2 = -1;
        switch(type.hashCode()) {
            case -894935028:
                if (type.equals("sqlite")) {
                    var2 = 1;
                }
                break;
            case 104382626:
                if (type.equals("mysql")) {
                    var2 = 0;
                }
        }

        switch(var2) {
            case 0:
                manager = new MySQL();
                break;
            case 1:
                manager = new SQLite();
                break;
            default:
                manager = null;
                SkyWars.getPlugin().getLogger().severe("Database type not supported!");
        }

    }

    public static DataSource getDS() {
        return manager;
    }
}
