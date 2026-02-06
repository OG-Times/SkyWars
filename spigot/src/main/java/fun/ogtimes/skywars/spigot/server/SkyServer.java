package fun.ogtimes.skywars.spigot.server;

import fun.ogtimes.skywars.spigot.SkyWars;
import fun.ogtimes.skywars.spigot.arena.Arena;
import fun.ogtimes.skywars.spigot.database.DatabaseHandler;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import fun.ogtimes.skywars.spigot.instance.InstanceModeHandler;
import org.bukkit.Bukkit;

public class SkyServer {
    public static String getProxyId() {
        return "SkyWars-Game-" + InstanceModeHandler.INSTANCE.getProperties().getId();
    }

    public static void load() {
        Bukkit.getScheduler().runTaskAsynchronously(SkyWars.getPlugin(), () -> DatabaseHandler.getDS().loadServer());
    }

    public static void setValues(Arena arena) {
        if (SkyWars.isServerEnabled()) {
            Bukkit.getScheduler().runTaskAsynchronously(SkyWars.getPlugin(), () -> DatabaseHandler.getDS().setServerData(arena));
            sendUpdateRequest();
        }

    }

    public static void sendUpdateRequest() {
        Bukkit.getScheduler().runTaskLater(SkyWars.getPlugin(), () -> {
            ByteArrayDataOutput var0 = ByteStreams.newDataOutput();
            var0.writeUTF(getProxyId());
            SkyWars.getPlugin().getServer().sendPluginMessage(SkyWars.getPlugin(), "skywars:sign-send", var0.toByteArray());
        }, 10L);
    }
}
