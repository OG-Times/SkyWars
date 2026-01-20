package fun.ogtimes.skywars.spigot.listener;

import fun.ogtimes.skywars.spigot.server.Server;
import fun.ogtimes.skywars.spigot.server.ServerManager;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

public class BungeeRecieveListener implements PluginMessageListener {
   public void onPluginMessageReceived(String var1, Player var2, byte[] var3) {
      if (var1.equals("SkyWars-Sign-Update")) {
         ByteArrayDataInput var4 = ByteStreams.newDataInput(var3);
         String var5 = var4.readUTF();
         Server var6 = ServerManager.getServer(var5);
         if (var6 != null) {
            var6.getData(true);
         }
      }
   }
}
