package fun.ogtimes.skywars.spigot.database.types;

import fun.ogtimes.skywars.spigot.SkyWars;
import fun.ogtimes.skywars.spigot.arena.Arena;
import fun.ogtimes.skywars.spigot.config.ConfigManager;
import fun.ogtimes.skywars.spigot.database.DataSource;
import fun.ogtimes.skywars.spigot.player.SkyPlayer;
import fun.ogtimes.skywars.spigot.server.Server;
import fun.ogtimes.skywars.spigot.server.ServerManager;
import fun.ogtimes.skywars.spigot.server.SkyServer;
import fun.ogtimes.skywars.spigot.utils.economy.skyeconomy.CustomEconomy;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
import lombok.SneakyThrows;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

public class MySQL extends DataSource {
    private final int port = ConfigManager.database.getInt("mysql.port", 3306);
    private final String host = ConfigManager.database.getString("mysql.host", "localhost");
    private final String database = ConfigManager.database.getString("mysql.database", "SkyWars");
    private final String username = ConfigManager.database.getString("mysql.user", "root");
    private final String password = ConfigManager.database.getString("mysql.password", "");
    private HikariDataSource ds;

    @SneakyThrows
    public MySQL() {
        try {
            this.setConnectionArguments();
        } catch (RuntimeException ex) {
            if (ex instanceof IllegalArgumentException) {
                SkyWars.logError("Invalid database arguments! Please check your configuration!");
                SkyWars.logError("If this error persists, please report it to the developer!");
                throw new IllegalArgumentException(ex);
            }

            if (ex instanceof HikariPool.PoolInitializationException) {
                SkyWars.logError("Can't initialize database connection! Please check your configuration!");
                SkyWars.logError("If this error persists, please report it to the developer!");
                throw new HikariPool.PoolInitializationException(ex);
            }

            SkyWars.logError("Can't use the Hikari Connection Pool! Please, report this error to the developer!");
            throw ex;
        }

        try {
            this.setupConnection();
        } catch (SQLException ex) {
            this.close();
            SkyWars.logError("Can't initialize the MySQL database... Please check your database settings in the config.yml file! SHUTDOWN...");
            SkyWars.logError("If this error persists, please report it to the developer!");
            throw ex;
        }
    }

    private synchronized void setConnectionArguments() {
        this.ds = new HikariDataSource();
        this.ds.setPoolName("SkyWars MySQL");
        boolean debugDb = ConfigManager.database.getBoolean("debug.debug-database", ConfigManager.main.getBoolean("debug-database", false));
        if (debugDb) {
            this.ds.setJdbcUrl("jdbc:log4jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database);
            this.ds.setDriverClassName("net.sf.log4jdbc.sql.jdbcapi.DriverSpy");
        } else {
            this.ds.setDriverClassName("com.mysql.jdbc.Driver");
            this.ds.setJdbcUrl("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database);
        }

        this.ds.addDataSourceProperty("cachePrepStmts", "true");
        this.ds.addDataSourceProperty("prepStmtCacheSize", "250");
        this.ds.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        this.ds.addDataSourceProperty("characterEncoding", "utf8");
        this.ds.addDataSourceProperty("encoding", "UTF-8");
        this.ds.addDataSourceProperty("useUnicode", "true");
        this.ds.addDataSourceProperty("useSSL", "false");
        this.ds.setUsername(this.username);
        this.ds.setPassword(this.password);
        long maxLifetime = ConfigManager.database.getLong("mysql.pool.maxLifetimeMs", 180000L);
        int minimumIdle = ConfigManager.database.getInt("mysql.pool.minimumIdle", 1);
        int maxPool = ConfigManager.database.getInt("mysql.pool.maxPoolSize", SkyWars.isProxyMode() ? 4 : 8);
        this.ds.setMaxLifetime(maxLifetime);
        this.ds.setIdleTimeout(60000L);
        this.ds.setMinimumIdle(minimumIdle);
        this.ds.setMaximumPoolSize(maxPool);
        SkyWars.log("Connection arguments loaded, Hikari ConnectionPool ready!");
    }

    @SneakyThrows
    public Connection getConnection() {
        return this.ds.getConnection();
    }

    private void setupConnection() throws SQLException {
        Connection var1 = this.getConnection();
        Throwable var2 = null;

        try {
            Statement statement = var1.createStatement();
            statement.executeUpdate(String.format("CREATE TABLE IF NOT EXISTS `%s` (`id` INT NOT NULL AUTO_INCREMENT, `username` VARCHAR(32) NOT NULL UNIQUE, `uuid` varchar(40) UNIQUE, `kits` TEXT, `abilities` TEXT, `last_colour` VARCHAR(32) DEFAULT NULL, `wins` INT(12) DEFAULT '0', `kills` INT(12) DEFAULT '0', `deaths` INT(12) DEFAULT '0', `played` INT(12) DEFAULT '0', `arrow_shot` INT(12) DEFAULT '0', `arrow_hit` INT(12) DEFAULT '0', `blocks_broken` INT(12) DEFAULT '0', `blocks_placed` INT(12) DEFAULT '0', `time_played` INT(12) DEFAULT '0', `distance_walked` INT(12) DEFAULT '0', PRIMARY KEY (id), KEY `swdata_username_idx` (`username`(32))) ENGINE=InnoDB;", this.TABLE_DATA));
            this.addColumn(this.TABLE_DATA, "uuid", "VARCHAR(255) NOT NULL UNIQUE", "id");
            this.addColumn(this.TABLE_DATA, "username", "VARCHAR(255) NOT NULL UNIQUE", "uuid");
            this.addColumn(this.TABLE_DATA, "kits", "TEXT", "uuid");
            this.addColumn(this.TABLE_DATA, "abilities", "TEXT", "kits");
            this.addColumn(this.TABLE_DATA, "last_colour", "VARCHAR(40)", "abilities");
            this.addColumn(this.TABLE_DATA, "wins", "INT(12) DEFAULT 0", "last_colour");
            this.addColumn(this.TABLE_DATA, "kills", "INT(12) DEFAULT 0", "wins");
            this.addColumn(this.TABLE_DATA, "deaths", "INT(12) DEFAULT 0", "kills");
            this.addColumn(this.TABLE_DATA, "played", "INT(12) DEFAULT 0", "deaths");
            this.addColumn(this.TABLE_DATA, "arrow_shot", "INT(12) DEFAULT 0", "played");
            this.addColumn(this.TABLE_DATA, "arrow_hit", "INT(12) DEFAULT 0", "arrow_shot");
            this.addColumn(this.TABLE_DATA, "blocks_broken", "INT(12) DEFAULT 0", "arrow_hit");
            this.addColumn(this.TABLE_DATA, "blocks_placed", "INT(12) DEFAULT 0", "blocks_broken");
            this.addColumn(this.TABLE_DATA, "time_played", "INT(12) DEFAULT 0", "blocks_placed");
            this.addColumn(this.TABLE_DATA, "distance_walked", "INT(12) DEFAULT 0", "time_played");
            statement.close();
            this.fixDuplicates();
            DatabaseMetaData var5 = var1.getMetaData();
            ResultSet var4 = var5.getIndexInfo(null, null, this.TABLE_DATA, true, false);
            boolean var6 = false;

            while(var4.next()) {
                String var7 = var4.getString("COLUMN_NAME");
                String var8 = var4.getString("INDEX_NAME");
                if (var8 != null && var8.startsWith("username_")) {
                    statement = var1.createStatement();
                    statement.executeUpdate(String.format("DROP INDEX %s ON %s", var8, this.TABLE_DATA));
                    statement.close();
                }

                if (var7 != null && var8 != null && var7.equalsIgnoreCase("username") && var8.equalsIgnoreCase("username")) {
                    var6 = true;
                }
            }

            var4.close();
            if (!var6) {
                statement = var1.createStatement();
                statement.executeUpdate(String.format("ALTER TABLE %s ADD UNIQUE (username);", this.TABLE_DATA));
                statement.close();
            }

            if (CustomEconomy.isCustom()) {
                statement = var1.createStatement();
                DatabaseMetaData var18 = var1.getMetaData();
                statement.executeUpdate(String.format("CREATE TABLE IF NOT EXISTS %s (id INT NOT NULL PRIMARY KEY AUTO_INCREMENT, uuid VARCHAR(40) UNIQUE, username VARCHAR(32), money DOUBLE(20,2) NOT NULL DEFAULT '0', KEY sweconomy_username_idx (username(32))) ENGINE=InnoDB;", this.TABLE_ECONOMY));
                statement.close();
                this.addColumn(this.TABLE_ECONOMY, "uuid", "VARCHAR(40)", "id");
                var4 = var18.getColumns(null, null, this.TABLE_ECONOMY, "money");
                if (var4.next()) {
                    int var19 = var4.getInt("DATA_TYPE");
                    if (var19 == 4) {
                        statement = var1.createStatement();
                        statement.executeUpdate(String.format("ALTER TABLE %s MODIFY money DOUBLE(20,2)", this.TABLE_ECONOMY));
                    }
                }

                var4.close();
                statement.close();
            }

            if (SkyWars.isServerEnabled() || SkyWars.isLobbyMode()) {
                statement = var1.createStatement();
                statement.executeUpdate(String.format("CREATE TABLE IF NOT EXISTS %s (id INT NOT NULL PRIMARY KEY AUTO_INCREMENT, bungeeid VARCHAR(40), players INT(6) DEFAULT '0', max_players INT(6) DEFAULT '0', map VARCHAR(32) DEFAULT NULL, state VARCHAR(32) DEFAULT NULL, loading INT(1) DEFAULT'0') ENGINE=InnoDB;", this.TABLE_SERVER));
                statement.close();
            }
        } catch (Throwable var16) {
            var2 = var16;
            throw var16;
        } finally {
            if (var1 != null) {
                if (var2 != null) {
                    try {
                        var1.close();
                    } catch (Throwable var15) {
                        var2.addSuppressed(var15);
                    }
                } else {
                    var1.close();
                }
            }

        }

        SkyWars.log("MySQL setup finished");
    }

    private void addColumn(String var1, String var2, String var3, String var4) {
        ResultSet var5 = null;
        Statement var6 = null;

        try {
            Connection var7 = this.getConnection();
            Throwable var8 = null;

            try {
                var6 = var7.createStatement();
                DatabaseMetaData var9 = var7.getMetaData();
                var5 = var9.getColumns(null, null, var1, var2);
                if (!var5.next()) {
                    var6.executeUpdate(String.format("ALTER TABLE %s ADD COLUMN %s %s AFTER %s;", var1, var2, var3, var4));
                }
            } catch (Throwable var26) {
                var8 = var26;
                throw var26;
            } finally {
                if (var7 != null) {
                    if (var8 != null) {
                        try {
                            var7.close();
                        } catch (Throwable var25) {
                            var8.addSuppressed(var25);
                        }
                    } else {
                        var7.close();
                    }
                }

            }
        } catch (SQLException var28) {
            var28.printStackTrace();
        } finally {
            this.close(var5);
            this.close(var6);
        }

    }

    public void close() {
        if (this.ds != null && !this.ds.isClosed()) {
            this.ds.close();
        }

    }

    public void loadPlayerData(SkyPlayer skyPlayer) {
        try {
            Connection var2 = this.getConnection();
            Throwable var3 = null;

            try {
                this.loadPlayerData(var2, skyPlayer);
            } catch (Throwable var13) {
                var3 = var13;
                throw var13;
            } finally {
                if (var2 != null) {
                    if (var3 != null) {
                        try {
                            var2.close();
                        } catch (Throwable var12) {
                            var3.addSuppressed(var12);
                        }
                    } else {
                        var2.close();
                    }
                }

            }
        } catch (SQLException var15) {
            var15.printStackTrace();
        }

    }

    public void uploadPlayerData(SkyPlayer skyPlayer) {
        try {
            Connection var2 = this.getConnection();
            Throwable var3 = null;

            try {
                this.uploadPlayerData(var2, skyPlayer);
            } catch (Throwable var13) {
                var3 = var13;
                throw var13;
            } finally {
                if (var2 != null) {
                    if (var3 != null) {
                        try {
                            var2.close();
                        } catch (Throwable var12) {
                            var3.addSuppressed(var12);
                        }
                    } else {
                        var2.close();
                    }
                }

            }
        } catch (SQLException var15) {
            var15.printStackTrace();
        }

    }

    public double getCoins(SkyPlayer skyPlayer) {
        try {
            Connection var2 = this.getConnection();
            Throwable var3 = null;

            double var4;
            try {
                var4 = this.getCoins(var2, skyPlayer);
            } catch (Throwable var15) {
                var3 = var15;
                throw var15;
            } finally {
                if (var2 != null) {
                    if (var3 != null) {
                        try {
                            var2.close();
                        } catch (Throwable var14) {
                            var3.addSuppressed(var14);
                        }
                    } else {
                        var2.close();
                    }
                }

            }

            return var4;
        } catch (SQLException var17) {
            var17.printStackTrace();
            return 0.0D;
        }
    }

    public void modifyCoins(SkyPlayer skyPlayer, double modifier) {
        try {
            Connection var4 = this.getConnection();
            Throwable var5 = null;

            try {
                this.modifyCoins(var4, skyPlayer, modifier);
            } catch (Throwable var15) {
                var5 = var15;
                throw var15;
            } finally {
                if (var4 != null) {
                    if (var5 != null) {
                        try {
                            var4.close();
                        } catch (Throwable var14) {
                            var5.addSuppressed(var14);
                        }
                    } else {
                        var4.close();
                    }
                }

            }
        } catch (SQLException var17) {
            var17.printStackTrace();
        }

    }

    public void loadServer() {
        PreparedStatement statement = null;
        ResultSet result = null;

        try {
            Connection connection = this.getConnection();
            Throwable throwable = null;

            try {
                statement = connection.prepareStatement(String.format("SELECT * FROM %s WHERE bungeeid=?", this.TABLE_SERVER));
                statement.setString(1, SkyServer.getProxyId());
                result = statement.executeQuery();
                if (!result.next()) {
                    statement.close();
                    statement = connection.prepareStatement(String.format("INSERT INTO %s (bungeeid) VALUES(?)", this.TABLE_SERVER));
                    statement.setString(1, SkyServer.getProxyId());
                    statement.executeUpdate();
                }
            } catch (Throwable e) {
                throwable = e;
                throw e;
            } finally {
                if (connection != null) {
                    if (throwable != null) {
                        try {
                            connection.close();
                        } catch (Throwable ex) {
                            throwable.addSuppressed(ex);
                        }
                    } else {
                        connection.close();
                    }
                }

            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.close(result);
            this.close(statement);
        }

    }

    public void getServers() {
        PreparedStatement var1 = null;
        ResultSet var2 = null;

        try {
            Connection var3 = this.getConnection();
            Throwable var4 = null;

            try {
                var1 = var3.prepareStatement(String.format("SELECT * FROM %s", this.TABLE_SERVER));
                var2 = var1.executeQuery();

                while(var2.next()) {
                    String var5 = var2.getString("bungeeid");
                    if (!ServerManager.servers.containsKey(var5)) {
                        ServerManager.servers.put(var5, new Server(var5));
                    }
                }
            } catch (Throwable var22) {
                var4 = var22;
                throw var22;
            } finally {
                if (var3 != null) {
                    if (var4 != null) {
                        try {
                            var3.close();
                        } catch (Throwable var21) {
                            var4.addSuppressed(var21);
                        }
                    } else {
                        var3.close();
                    }
                }

            }
        } catch (SQLException var24) {
            var24.printStackTrace();
        } finally {
            this.close(var2);
            this.close(var1);
        }

    }

    public void setServerData(Arena arena) {
        PreparedStatement statement = null;

        try {
            Connection connection = this.getConnection();
            Throwable var4 = null;

            try {
                statement = connection.prepareStatement(String.format("UPDATE %s SET players=?, max_players=?, map=?, loading=?, state=? WHERE bungeeid=?", this.TABLE_SERVER));
                statement.setInt(1, arena.getAlivePlayers());
                statement.setInt(2, arena.getMaxPlayers());
                statement.setString(3, arena.getDisplayName());
                statement.setInt(4, arena.isLoading() ? 1 : 0);
                statement.setString(5, arena.getState().toString());
                statement.setString(6, SkyServer.getProxyId());
                statement.executeUpdate();
            } catch (Throwable var22) {
                var4 = var22;
                throw var22;
            } finally {
                if (connection != null) {
                    if (var4 != null) {
                        try {
                            connection.close();
                        } catch (Throwable var21) {
                            var4.addSuppressed(var21);
                        }
                    } else {
                        connection.close();
                    }
                }

            }
        } catch (SQLException var24) {
            var24.printStackTrace();
        } finally {
            this.close(statement);
        }

    }

    public List<Entry<String, Integer>> getTopStats(String var1, int var2) {
        try {
            Connection var3 = this.getConnection();
            Throwable var4 = null;

            List var5;
            try {
                var5 = this.getTopStats(var3, var1, var2);
            } catch (Throwable var15) {
                var4 = var15;
                throw var15;
            } finally {
                if (var3 != null) {
                    if (var4 != null) {
                        try {
                            var3.close();
                        } catch (Throwable var14) {
                            var4.addSuppressed(var14);
                        }
                    } else {
                        var3.close();
                    }
                }

            }

            return var5;
        } catch (SQLException var17) {
            var17.printStackTrace();
            return new ArrayList<>();
        }
    }

    private void fixDuplicates() {
        PreparedStatement var1 = null;
        PreparedStatement var2 = null;
        ResultSet var3 = null;
        ResultSet var4 = null;
        int var5 = 0;

        try {
            Connection var6 = this.getConnection();
            Throwable var7 = null;

            try {
                var1 = var6.prepareStatement(String.format("SELECT username, COUNT(username) FROM %s GROUP BY username HAVING COUNT(username) > 1;", this.TABLE_DATA));

                label288:
                for(var3 = var1.executeQuery(); var3.next(); ++var5) {
                    String var8 = var3.getString("username");
                    int var18 = 0;
                    int var17 = 0;
                    int var16 = 0;
                    int var15 = 0;
                    int var14 = 0;
                    int var13 = 0;
                    int var12 = 0;
                    int var11 = 0;
                    int var10 = 0;
                    int var9 = 0;
                    String var19 = null;
                    ArrayList var20 = new ArrayList();
                    var2 = var6.prepareStatement(String.format("SELECT * FROM %s WHERE username=?;", this.TABLE_DATA));
                    var2.setString(1, var8);
                    var4 = var2.executeQuery();

                    while(true) {
                        String var21;
                        do {
                            do {
                                if (!var4.next()) {
                                    var4.close();
                                    var2.close();
                                    var2 = var6.prepareStatement(String.format("DELETE t1 FROM %s t1 INNER JOIN %s t2 WHERE t1.id > t2.id AND t1.username = t2.username;", this.TABLE_DATA, this.TABLE_DATA));
                                    var2.executeUpdate();
                                    var2.close();
                                    var2 = var6.prepareStatement(String.format("UPDATE %s SET uuid=?, kits=?, wins=?, kills=?, deaths=?, played=?, arrow_shot=?, arrow_hit=?, blocks_broken=?, blocks_placed=?, time_played=?, distance_walked=? WHERE username=?", this.TABLE_DATA));
                                    StringBuilder var47 = new StringBuilder();

                                    for (Object object : var20) {
                                        String var49 = (String) object;
                                        var47.append(var49).append(",");
                                    }

                                    var2.setString(1, var19);
                                    var2.setString(2, var47.toString());
                                    var2.setInt(3, var9);
                                    var2.setInt(4, var10);
                                    var2.setInt(5, var11);
                                    var2.setInt(6, var12);
                                    var2.setInt(7, var13);
                                    var2.setInt(8, var14);
                                    var2.setInt(9, var15);
                                    var2.setInt(10, var16);
                                    var2.setInt(11, var17);
                                    var2.setInt(12, var18);
                                    var2.setString(13, var8);
                                    var2.executeUpdate();
                                    var2.close();
                                    continue label288;
                                }

                                if (var19 == null) {
                                    var19 = var4.getString("uuid");
                                }

                                var9 += var4.getInt("wins");
                                var10 += var4.getInt("kills");
                                var11 += var4.getInt("deaths");
                                var12 += var4.getInt("played");
                                var13 += var4.getInt("arrow_shot");
                                var14 += var4.getInt("arrow_hit");
                                var15 += var4.getInt("blocks_broken");
                                var16 += var4.getInt("blocks_placed");
                                var17 += var4.getInt("time_played");
                                var18 += var4.getInt("distance_walked");
                                var21 = var4.getString("kits");
                            } while(var21 == null);
                        } while(var21.isEmpty());

                        String[] var22 = var21.split(",");
                        int var24 = var22.length;

                        for(int var25 = 0; var25 < var24; ++var25) {
                            String var26 = var22[var25];
                            if (!var20.contains(var26)) {
                                var20.add(var26);
                            }
                        }
                    }
                }
            } catch (Throwable var43) {
                var7 = var43;
                throw var43;
            } finally {
                if (var6 != null) {
                    if (var7 != null) {
                        try {
                            var6.close();
                        } catch (Throwable var42) {
                            var7.addSuppressed(var42);
                        }
                    } else {
                        var6.close();
                    }
                }

            }
        } catch (SQLException var45) {
            var45.printStackTrace();
        } finally {
            this.close(var3);
            this.close(var4);
            this.close(var1);
        }

        if (var5 > 0) {
            SkyWars.console("&e" + var5 + " entries were fixed from duplicate rows in &a" + this.TABLE_DATA);
        }

    }
}
