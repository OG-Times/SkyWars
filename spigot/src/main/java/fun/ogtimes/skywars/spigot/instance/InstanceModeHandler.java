package fun.ogtimes.skywars.spigot.instance;

import com.avaje.ebean.OrderBy;
import fun.ogtimes.skywars.spigot.SkyWars;
import fun.ogtimes.skywars.spigot.config.ConfigManager;
import fun.ogtimes.skywars.common.SkyWarsCommon;
import fun.ogtimes.skywars.common.instance.InstanceProperties;
import fun.ogtimes.skywars.common.nats.packet.impl.InstanceStartPacket;
import fun.ogtimes.skywars.common.nats.packet.impl.InstanceStopPacket;
import fun.ogtimes.skywars.common.nats.packet.impl.KeepAlivePacket;
import io.nats.client.Options;
import lombok.Getter;
import org.bukkit.Bukkit;

import java.io.*;
import java.time.Duration;

@Getter
public class InstanceModeHandler {

    public static InstanceModeHandler INSTANCE;
    private final SkyWars plugin;
    private SkyWarsCommon common;
    private InstanceProperties properties;

    public InstanceModeHandler(SkyWars plugin) {
        this.plugin = plugin;
        INSTANCE = this;

        String address = Bukkit.getServer().getIp().isEmpty() ? "0.0.0.0" : Bukkit.getServer().getIp();
        int port = Bukkit.getServer().getPort();

        String id;
        String devFlag = System.getProperty("skywars.dev");
        if (devFlag != null && devFlag.equalsIgnoreCase("true")) {
            id = "Staging";
        } else {
            String sysId = System.getProperty("instance.id");
            if (sysId != null && !sysId.isEmpty()) {
                id = sysId.replace("skywars-", "");
            } else {
                throw new IllegalStateException("Instance ID not set! Please set the 'instance.id' system property.");
            }
        }

        this.properties = new InstanceProperties(id, address, port);
    }

    public void start() {
        if (!SkyWars.isMultiArenaMode()) return;

        String natsUrl = "nats://127.0.0.1:4222";
        try {
            if (ConfigManager.database != null && ConfigManager.database.isSet("nats.url")) {
                natsUrl = ConfigManager.database.getString("nats.url", natsUrl);
            } else if (ConfigManager.main != null && ConfigManager.main.isSet("nats.url")) {
                natsUrl = ConfigManager.main.getString("nats.url", natsUrl);
            }
        } catch (Exception ignored) {
        }

        try {
            Options opts = Options.builder().server(natsUrl).connectionTimeout(Duration.ofSeconds(5)).build();
            this.common = new SkyWarsCommon(opts);
        } catch (Exception e) {
            SkyWars.logError("Failed to initialize SkyWarsCommon NATS connection: " + e.getMessage());
            return;
        }

        try {
            this.common.nats().sendPacket(new InstanceStartPacket(this.properties));
            try {
                this.common.nats().sendPacket(new KeepAlivePacket(this.properties));
                SkyWars.log("Sent initial KeepAlive for instance " + this.properties.getId());
            } catch (Exception ex) {
                SkyWars.logError("Failed to send initial KeepAlive: " + ex.getMessage());
            }

            this.common.keepAlive().start(this.properties);
            SkyWars.log("Instance announced to NATS with id " + this.properties.getId() + " (" + this.properties.getAddress() + ":" + this.properties.getPort() + ")");
        } catch (Exception e) {
            SkyWars.logError("Failed to announce instance start: " + e.getMessage());
        }
    }

    public void stop() {
        if (this.common == null || this.properties == null) return;

        try {
            this.common.nats().sendPacket(new InstanceStopPacket(this.properties));
        } catch (Exception e) {
            SkyWars.logError("Failed to send InstanceStopPacket: " + e.getMessage());
        }

        try {
            this.common.destroy();
        } catch (Exception e) {
            SkyWars.logError("Failed to destroy SkyWarsCommon: " + e.getMessage());
        }
    }

}
