package fun.ogtimes.skywars.spigot.database;

import fun.ogtimes.skywars.spigot.SkyWars;
import fun.ogtimes.skywars.spigot.arena.Arena;
import fun.ogtimes.skywars.spigot.kit.Kit;
import fun.ogtimes.skywars.spigot.kit.KitManager;
import fun.ogtimes.skywars.spigot.player.SkyPlayer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;

public abstract class DataSource {
    public final String TABLE_DATA = SkyWars.getPlugin().getConfig().getString("data.mysql.tablename.data");
    public final String TABLE_ECONOMY = SkyWars.getPlugin().getConfig().getString("data.mysql.tablename.economy");
    public final String TABLE_SERVER = SkyWars.getPlugin().getConfig().getString("data.mysql.tablename.servers");

    protected DataSource() {
    }

    public abstract void close();

    public void close(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception ignored) {
            }
        }

    }

    public abstract Connection getConnection();

    public abstract void loadPlayerData(SkyPlayer skyPlayer);

    public abstract void uploadPlayerData(SkyPlayer skyPlayer);

    public abstract double getCoins(SkyPlayer skyPlayer);

    public abstract void modifyCoins(SkyPlayer skyPlayer, double modifier);

    public abstract void loadServer();

    public abstract void getServers();

    public abstract void setServerData(Arena arena);

    public abstract List<Entry<String, Integer>> getTopStats(String var1, int var2);

    protected void loadPlayerData(Connection connection, SkyPlayer skyPlayer) {
        PreparedStatement statement = null;
        ResultSet result = null;

        try {
            statement = connection.prepareStatement(String.format("SELECT * FROM %s WHERE uuid=? OR (uuid IS NULL AND username=?) OR (username=?)", this.TABLE_DATA));
            statement.setString(1, skyPlayer.getUniqueId().toString());
            statement.setString(2, skyPlayer.getName());
            statement.setString(3, skyPlayer.getName());
            result = statement.executeQuery();
            if (result.next()) {
                String kits;
                if (result.getString("kits") != null) {
                    kits = result.getString("kits");

                    for (Kit kit : KitManager.getKits()) {
                        if (kits.contains(kit.getName())) {
                            skyPlayer.addKit(kit);
                        }
                    }
                }

                if (result.getString("last_colour") != null) {
                    skyPlayer.setBoxSection(result.getString("last_colour"), false);
                } else {
                    skyPlayer.setBoxSection(SkyWars.boxes.getString("default"), false);
                }

                kits = result.getString("abilities");
                if (kits != null && !kits.isEmpty()) {
                    skyPlayer.serializeAbilities(kits);
                }

                skyPlayer.setWins(result.getInt("wins"));
                skyPlayer.setKills(result.getInt("kills"));
                skyPlayer.setDeaths(result.getInt("deaths"));
                skyPlayer.setPlayed(result.getInt("played"));
                skyPlayer.setArrowShot(result.getInt("arrow_shot"));
                skyPlayer.setArrowHit(result.getInt("arrow_hit"));
                skyPlayer.setBlocksBroken(result.getInt("blocks_broken"));
                skyPlayer.setBlocksPlaced(result.getInt("blocks_placed"));
                skyPlayer.setTimePlayed(result.getInt("time_played"));
                skyPlayer.setDistanceWalked(result.getInt("distance_walked"));
                if (result.getString("uuid") == null || result.getString("uuid").isEmpty()) {
                    skyPlayer.addData("upload_data", true);
                }
            } else {
                result.close();
                statement.close();
                statement = connection.prepareStatement(String.format("INSERT INTO %s (uuid,username) VALUES (?,?)", this.TABLE_DATA));
                statement.setString(1, skyPlayer.getUniqueId().toString());
                statement.setString(2, skyPlayer.getName());
                statement.executeUpdate();
                skyPlayer.setBoxSection(SkyWars.boxes.getString("default"), false);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            this.close(result);
            this.close(statement);
        }

    }

    protected void uploadPlayerData(Connection connection, SkyPlayer skyPlayer) {
        PreparedStatement statement = null;

        try {
            statement = connection.prepareStatement(String.format("UPDATE %s SET username=?, uuid=?, kits=?, abilities=?, last_colour=?,  wins=?, kills=?, deaths=?, played=?, arrow_shot=?, arrow_hit=?, blocks_broken=?, blocks_placed=?, time_played=?, distance_walked=? WHERE uuid=? OR (uuid IS NULL AND username=?)", this.TABLE_DATA));
            statement.setString(1, skyPlayer.getName());
            statement.setString(2, skyPlayer.getUniqueId().toString());
            statement.setString(3, skyPlayer.convertKitsToString());
            statement.setString(4, skyPlayer.deserializeAbilities());
            statement.setString(5, skyPlayer.getBoxSection());
            statement.setInt(6, skyPlayer.getWins());
            statement.setInt(7, skyPlayer.getKills());
            statement.setInt(8, skyPlayer.getDeaths());
            statement.setInt(9, skyPlayer.getPlayed());
            statement.setInt(10, skyPlayer.getArrowShot());
            statement.setInt(11, skyPlayer.getArrowHit());
            statement.setInt(12, skyPlayer.getBlocksBroken());
            statement.setInt(13, skyPlayer.getBlocksPlaced());
            statement.setInt(14, skyPlayer.getTimePlayed());
            statement.setInt(15, skyPlayer.getDistanceWalked());
            statement.setString(16, skyPlayer.getUniqueId().toString());
            statement.setString(17, skyPlayer.getName());
            statement.executeUpdate();
            skyPlayer.addData("upload_data", false);
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            this.close(statement);
        }

    }

    protected double getCoins(Connection connection, SkyPlayer skyPlayer) {
        double var3 = 0.0D;
        PreparedStatement var5 = null;
        ResultSet var6 = null;

        try {
            var5 = connection.prepareStatement(String.format("SELECT money FROM %s WHERE uuid=? OR (uuid IS NULL AND username=?)", this.TABLE_ECONOMY));
            var5.setString(1, skyPlayer.getUniqueId().toString());
            var5.setString(2, skyPlayer.getName());
            var6 = var5.executeQuery();
            if (var6.next()) {
                var3 = var6.getDouble("money");
            } else {
                var6.close();
                var5.close();
                var5 = connection.prepareStatement(String.format("INSERT INTO %s (uuid,username) VALUES (?,?)", this.TABLE_ECONOMY));
                var5.setString(1, skyPlayer.getUniqueId().toString());
                var5.setString(2, skyPlayer.getName());
                var5.executeUpdate();
            }
        } catch (SQLException var11) {
            var11.printStackTrace();
        } finally {
            this.close(var6);
            this.close(var5);
        }

        return var3;
    }

    protected void modifyCoins(Connection var1, SkyPlayer var2, double var3) {
        PreparedStatement var5 = null;

        try {
            var5 = var1.prepareStatement(String.format("UPDATE %s SET money=?, uuid=?, username=? WHERE uuid=? OR (uuid IS NULL AND username=?)", this.TABLE_ECONOMY));
            var5.setDouble(1, var3);
            var5.setString(2, var2.getUniqueId().toString());
            var5.setString(3, var2.getName());
            var5.setString(4, var2.getUniqueId().toString());
            var5.setString(5, var2.getName());
            var5.executeUpdate();
        } catch (SQLException var10) {
            var10.printStackTrace();
        } finally {
            this.close(var5);
        }

    }

    protected List<Entry<String, Integer>> getTopStats(Connection var1, String var2, int var3) {
        ArrayList var4 = new ArrayList();
        PreparedStatement var5 = null;
        ResultSet var6 = null;

        try {
            var5 = var1.prepareStatement(String.format("SELECT * FROM %s ORDER BY ? DESC LIMIT ?", this.TABLE_DATA));
            var5.setString(1, var2);
            var5.setInt(2, var3);
            var6 = var5.executeQuery();

            while(var6.next()) {
                var4.add(new SimpleEntry<>(var6.getString("username"), var6.getInt(var2)));
            }
        } catch (SQLException var11) {
            var11.printStackTrace();
        } finally {
            this.close(var6);
            this.close(var5);
        }

        return var4;
    }
}
