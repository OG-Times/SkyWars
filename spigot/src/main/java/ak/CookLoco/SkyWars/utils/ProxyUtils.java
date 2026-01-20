package ak.CookLoco.SkyWars.utils;

import ak.CookLoco.SkyWars.SkyWars;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ProxyUtils {
    public static void teleToServer(Player player, String message, String server) {
        if (!message.equalsIgnoreCase("")) {
            player.sendMessage(message);
        }

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
            dataOutputStream.writeUTF("Connect");
            dataOutputStream.writeUTF(server);
            player.sendPluginMessage(SkyWars.getPlugin(), "BungeeCord", outputStream.toByteArray());
            outputStream.close();
            dataOutputStream.close();
        } catch (Exception var5) {
            player.sendMessage(ChatColor.GOLD + "Error: Couldn't sent you to " + ChatColor.RED + server);
        }

    }
}
