package fun.ogtimes.skywars.spigot.database2;

import fun.ogtimes.skywars.spigot.SkyWars;
import fun.ogtimes.skywars.spigot.database2.types.MySQL;
import fun.ogtimes.skywars.spigot.database2.types.SQLite;

import java.sql.SQLException;

public class DatabaseHandler {
    private static DataSource manager;

    public DatabaseHandler() throws SQLException, ClassNotFoundException {
        String var1 = SkyWars.getPlugin().getConfig().getString("data.type").toLowerCase();
        byte var2 = -1;
        switch(var1.hashCode()) {
            case -894935028:
                if (var1.equals("sqlite")) {
                    var2 = 1;
                }
                break;
            case 104382626:
                if (var1.equals("mysql")) {
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
