package ak.CookLoco.SkyWars.server;

import ak.CookLoco.SkyWars.SkyWars;
import ak.CookLoco.SkyWars.arena.Arena;
import ak.CookLoco.SkyWars.database2.DatabaseHandler;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;

public class SkyServer {
   public static String getProxyId() {
      return SkyWars.getPlugin().getConfig().getString("server.bungeeid");
   }

   public static void load() {
      Bukkit.getScheduler().runTaskAsynchronously(SkyWars.getPlugin(), () -> {
         DatabaseHandler.getDS().loadServer();
      });
   }

   public static void setValues(Arena var0) {
      if (SkyWars.isServerEnabled()) {
         Bukkit.getScheduler().runTaskAsynchronously(SkyWars.getPlugin(), () -> {
            DatabaseHandler.getDS().setServerData(var0);
         });
         sendUpdateRequest();
      }

   }

   public static void sendUpdateRequest() {
      Bukkit.getScheduler().runTaskLater(SkyWars.getPlugin(), () -> {
         ByteArrayDataOutput var0 = ByteStreams.newDataOutput();
         var0.writeUTF(getProxyId());
         SkyWars.getPlugin().getServer().sendPluginMessage(SkyWars.getPlugin(), "SkyWars-Sign-Send", var0.toByteArray());
      }, 10L);
   }
}
