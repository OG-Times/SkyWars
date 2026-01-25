package fun.ogtimes.skywars.spigot.server;

import fun.ogtimes.skywars.spigot.SkyWars;
import fun.ogtimes.skywars.spigot.database.DatabaseHandler;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.bukkit.Bukkit;

public class ServerManager {
   public static final Map<String, Server> servers = new HashMap<>();

   public static void initServers() {
      DatabaseHandler.getDS().getServers();
      if (SkyWars.getMysql()) {
         Bukkit.getScheduler().runTaskTimerAsynchronously(SkyWars.getPlugin(), () -> {
            if (!SkyWars.disabling) {

                for (Server server : getServers()) {
                    server.getData(true);
                }
            }

         }, 0L, 15L);
      }

   }

   public static Set<Server> getServers() {
      return Collections.unmodifiableSet(Sets.newHashSet(servers.values()));
   }

   public static Server getServer(String server) {
      return servers.get(server);
   }
}
