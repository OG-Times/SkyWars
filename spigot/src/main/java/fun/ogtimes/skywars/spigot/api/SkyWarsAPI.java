package fun.ogtimes.skywars.spigot.api;

import fun.ogtimes.skywars.spigot.SkyWars;
import fun.ogtimes.skywars.spigot.database.DatabaseHandler;
import fun.ogtimes.skywars.spigot.player.SkyPlayer;
import java.util.List;
import java.util.Map.Entry;

import lombok.experimental.UtilityClass;
import org.bukkit.entity.Player;

@UtilityClass
public class SkyWarsAPI {
    public SkyPlayer getSkyPlayer(Player player) {
        return SkyWars.getSkyPlayer(player);
    }

    public int getWins(Player player) {
        SkyPlayer skyPlayer = SkyWars.getSkyPlayer(player);
        return skyPlayer.getWins();
    }

    public int getKills(Player player) {
        SkyPlayer skyPlayer = SkyWars.getSkyPlayer(player);
        return skyPlayer.getKills();
    }

    public int getDeaths(Player player) {
        SkyPlayer skyPlayer = SkyWars.getSkyPlayer(player);
        return skyPlayer.getDeaths();
    }

    public int getPlayed(Player player) {
        SkyPlayer skyPlayer = SkyWars.getSkyPlayer(player);
        return skyPlayer.getPlayed();
    }

    public List<Entry<String, Integer>> getTopWins(int number) {
        return DatabaseHandler.getDS().getTopStats("wins", number);
    }

    public List<Entry<String, Integer>> getTopKills(int number) {
        return DatabaseHandler.getDS().getTopStats("kills", number);
    }

    public List<Entry<String, Integer>> getTopDeaths(int number) {
        return DatabaseHandler.getDS().getTopStats("deaths", number);
    }

    public List<Entry<String, Integer>> getTopPlayed(int number) {
        return DatabaseHandler.getDS().getTopStats("played", number);
    }
}