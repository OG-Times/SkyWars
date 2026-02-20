package fun.ogtimes.skywars.velocity;

import javax.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import fun.ogtimes.skywars.common.keepalive.KeepAliveListener;
import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;

import fun.ogtimes.skywars.common.SkyWarsCommon;
import fun.ogtimes.skywars.common.instance.InstanceProperties;
import fun.ogtimes.skywars.common.nats.annotation.IncomingPacketHandler;
import fun.ogtimes.skywars.common.nats.packet.PacketListener;
import fun.ogtimes.skywars.common.nats.packet.impl.InstanceStartPacket;
import fun.ogtimes.skywars.common.nats.packet.impl.InstanceStopPacket;

import java.io.*;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;

import io.nats.client.Options;

/**
 * This code was made by jsexp, in case of any unauthorized
 * use, at least please leave credits.
 * Find more about me @ my <a href="https://github.com/hardcorefactions">GitHub</a> :D
 * © 2025 - jsexp
 */
@Plugin(
        id = "skywars",
        name = "SkyWars",
        version = "1.0.0",
        description = "CookLoco's SkyWars plugin for Velocity",
        authors = {"OGTimes Development Team"}
)
public class SkyWarsVelocity {
     private final ProxyServer proxy;
     private final Logger logger;
     private final Path dataDirectory;

    private static final MinecraftChannelIdentifier SIGN_SEND_CHANNEL =
            MinecraftChannelIdentifier.from("skywars:sign-send");
    private static final MinecraftChannelIdentifier SIGN_UPDATE_CHANNEL =
            MinecraftChannelIdentifier.from("skywars:sign-update");

    private Map<String, Object> config;
    private List<String> skywarsLobbies;

    private SkyWarsCommon common;
    private final Set<String> addedServers = new HashSet<>();

    private PacketListener instancePacketListener;
    private KeepAliveListener keepAliveListener;

    @Inject
    public SkyWarsVelocity(ProxyServer proxy, Logger logger, @DataDirectory Path dataDirectory) {
        this.proxy = proxy;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        proxy.getChannelRegistrar().register(SIGN_SEND_CHANNEL, SIGN_UPDATE_CHANNEL);

        loadConfig();
        initCommon();

        logger.info("SkyWars Velocity plugin has been enabled!");
    }

    private void initCommon() {
        String natsUrl = "nats://127.0.0.1:4222";
        if (config != null && config.containsKey("nats_url")) {
            Object o = config.get("nats_url");
            if (o instanceof String) natsUrl = (String) o;
        }

        try {
            Options opts = Options.builder().server(natsUrl).connectionTimeout(Duration.ofSeconds(5)).build();
            common = new SkyWarsCommon(opts);

            instancePacketListener = new PacketListener() {
                @IncomingPacketHandler
                public void onInstanceStart(InstanceStartPacket packet) {
                    if (packet == null || packet.properties() == null) return;
                    InstanceProperties props = packet.properties();
                    String id = props.getId();
                    String serverName = "SkyWars-Game-" + id;

                    try {
                        InetSocketAddress addr = new InetSocketAddress(props.getAddress(), props.getPort());
                        com.velocitypowered.api.proxy.server.ServerInfo info = new com.velocitypowered.api.proxy.server.ServerInfo(serverName, addr);
                        RegisteredServer reg = proxy.registerServer(info);
                        addedServers.add(serverName);
                        logger.info("Registered instance server '{}' -> {}:{}", serverName, props.getAddress(), props.getPort());

                    } catch (Exception e) {
                        logger.error("Failed to register instance server {}", serverName, e);
                    }
                }

                @IncomingPacketHandler
                public void onInstanceStop(InstanceStopPacket packet) {
                    if (packet == null || packet.properties() == null) return;
                    InstanceProperties props = packet.properties();
                    String id = props.getId();
                    String serverName = "SkyWars-Game-" + id;

                    try {
                        Optional<RegisteredServer> serverOpt = proxy.getServer(serverName);
                        if (serverOpt.isPresent()) {
                            RegisteredServer server = serverOpt.get();
                            boolean ok = tryUnregister(server, serverName);
                            if (!ok) {
                                logger.warn("Could not fully unregister server '{}', leaving it registered", serverName);
                            } else {
                                addedServers.remove(serverName);
                            }
                        }
                    } catch (Exception e) {
                        logger.error("Failed to unregister instance server {}", serverName, e);
                    }
                }
            };
            common.nats().registerListener(instancePacketListener);

            keepAliveListener = new fun.ogtimes.skywars.common.keepalive.KeepAliveListener() {
                @Override
                public void onInstanceAlive(InstanceProperties properties) {
                    String id = properties.getId();
                    String serverName = "SkyWars-Game-" + id;
                    try {
                        Optional<RegisteredServer> existing = proxy.getServer(serverName);
                        if (existing.isPresent()) return;
                        addedServers.add(serverName);
                        logger.info("Re-registered instance server '{}' from keepalive -> {}:{}", serverName, properties.getAddress(), properties.getPort());
                    } catch (Exception e) {
                        logger.error("Failed to re-register instance server {} on keepalive", serverName, e);
                    }
                }

                @Override
                public void onInstanceOffline(InstanceProperties properties) {
                    String id = properties.getId();
                    String serverName = "SkyWars-Game-" + id;
                    try {
                        Optional<RegisteredServer> serverOpt = proxy.getServer(serverName);
                        if (serverOpt.isPresent()) {
                            RegisteredServer server = serverOpt.get();
                            boolean ok = tryUnregister(server, serverName);
                            if (!ok) {
                                logger.warn("Could not fully unregister server '{}' after keepalive timeout, leaving it registered", serverName);
                            } else {
                                addedServers.remove(serverName);
                                logger.info("Unregistered instance server '{}' due to keepalive timeout", serverName);
                            }
                        }
                    } catch (Exception e) {
                        logger.error("Failed to handle offline instance server {} on keepalive timeout", serverName, e);
                    }
                }
            };
            common.keepAlive().registerListener(keepAliveListener);

            try {
                common.nats().subscribe();
            } catch (Exception e) {
                logger.error("Failed to subscribe to NATS channels", e);
            }

            try {
                common.keepAlive().startListening();
            } catch (Exception ignored) {}

            logger.info("Connected to NATS at {} and registered instance handlers", natsUrl);
        } catch (Exception e) {
            logger.error("Failed to init common/NATS connection: {}", e.getMessage(), e);
        }
    }

    @Subscribe
    public void onPluginMessage(PluginMessageEvent event) {
        if (!(event.getSource() instanceof ServerConnection)) {
            return;
        }

        if (!event.getIdentifier().equals(SIGN_SEND_CHANNEL)) {
            return;
        }

        try {
            byte[] data = event.getData();
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(data));
            String message = in.readUTF();
            sendUpdate(message);
        } catch (IOException e) {
            logger.error("Error reading plugin message", e);
        }
    }

    public void sendUpdate(String data) {
        for (String serverName : skywarsLobbies) {
            Optional<RegisteredServer> serverOpt = proxy.getServer(serverName);

            if (serverOpt.isEmpty()) {
                logger.warn("Server '{}' not found in proxy configuration", serverName);
                continue;
            }

            RegisteredServer server = serverOpt.get();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(baos);

            try {
                out.writeUTF(data);
                server.sendPluginMessage(SIGN_UPDATE_CHANNEL, baos.toByteArray());
            } catch (IOException e) {
                logger.error("An I/O error occurred while sending update to server '{}'", serverName, e);
            }
        }
    }

    private void loadConfig() {
        try {
            if (!Files.exists(dataDirectory)) {
                Files.createDirectories(dataDirectory);
            }

            Path configPath = dataDirectory.resolve("config.yml");

            if (!Files.exists(configPath)) {
                createDefaultConfig(configPath);
            }

            Yaml yaml = new Yaml();
            try (InputStream inputStream = Files.newInputStream(configPath)) {
                config = yaml.load(inputStream);
            }

            if (config != null && config.containsKey("skywarslobbies_servers")) {
                Object serversObj = config.get("skywarslobbies_servers");
                if (serversObj instanceof List) {
                    skywarsLobbies = (List<String>) serversObj;
                } else {
                    skywarsLobbies = new ArrayList<>();
                    logger.warn("Invalid skywarslobbies_servers configuration, using empty list");
                }
            } else {
                skywarsLobbies = new ArrayList<>();
                skywarsLobbies.add("SkyWarsLobby1");
                skywarsLobbies.add("SkyWarsLobby2");
                config = new HashMap<>();
                config.put("skywarslobbies_servers", skywarsLobbies);
                saveConfig(configPath);
            }

            logger.info("Loaded {} SkyWars lobby servers", skywarsLobbies.size());

        } catch (IOException e) {
            logger.error("Error loading configuration", e);
            skywarsLobbies = new ArrayList<>();
        }
    }

    private void createDefaultConfig(Path configPath) throws IOException {
        Map<String, Object> defaultConfig = new HashMap<>();
        List<String> defaultServers = new ArrayList<>();
        defaultServers.add("SkyWarsLobby1");
        defaultServers.add("SkyWarsLobby2");
        defaultConfig.put("skywarslobbies_servers", defaultServers);
        defaultConfig.put("nats_url", "nats://127.0.0.1:4222");

        Yaml yaml = new Yaml();
        try (Writer writer = Files.newBufferedWriter(configPath)) {
            yaml.dump(defaultConfig, writer);
        }

        logger.info("Created default configuration file");
    }

    private void saveConfig(Path configPath) throws IOException {
        Yaml yaml = new Yaml();
        try (Writer writer = Files.newBufferedWriter(configPath)) {
            yaml.dump(config, writer);
        }
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        try {
            if (common != null) {
                try {
                    if (keepAliveListener != null) {
                        common.keepAlive().unregisterListener(keepAliveListener);
                        common.keepAlive().stopListening();
                    }
                } catch (Exception ignored) {}
                try {
                    if (instancePacketListener != null) common.nats().unregisterListener(instancePacketListener);
                } catch (Exception ignored) {}
                try {
                    common.destroy();
                } catch (Exception ignored) {}
            }
        } catch (Exception ex) {
            logger.error("Error while shutting down NATS handlers", ex);
        }
    }

    private boolean tryUnregister(RegisteredServer server, String serverName) {
        try {
            Method m = proxy.getClass().getMethod("unregisterServer", com.velocitypowered.api.proxy.server.ServerInfo.class);
            m.invoke(proxy, server.getServerInfo());
            logger.info("Unregistered instance server '{}' via unregister(ServerInfo)", serverName);
            return true;
        } catch (NoSuchMethodException ignored) {
        } catch (Exception e) {
            logger.warn("unregister(ServerInfo) failed for '{}': {}", serverName, e.getMessage());
        }

        try {
            Method m = proxy.getClass().getMethod("unregisterServer", RegisteredServer.class);
            m.invoke(proxy, server);
            logger.info("Unregistered instance server '{}' via unregister(RegisteredServer)", serverName);
            return true;
        } catch (NoSuchMethodException ignored) {
        } catch (Exception e) {
            logger.warn("unregister(RegisteredServer) failed for '{}': {}", serverName, e.getMessage());
        }

        try {
            Method m = proxy.getClass().getMethod("unregisterServer", String.class);
            m.invoke(proxy, server.getServerInfo().getName());
            logger.info("Unregistered instance server '{}' via unregister(String)", serverName);
            return true;
        } catch (NoSuchMethodException ignored) {
        } catch (Exception e) {
            logger.warn("unregister(String) failed for '{}': {}", serverName, e.getMessage());
        }

        try {
            Collection<RegisteredServer> all = proxy.getAllServers();
            boolean removed = all.remove(server);
            if (removed) {
                logger.info("Removed instance server '{}' from server list (collection remove)", serverName);
                return true;
            }
        } catch (UnsupportedOperationException uoe) {
            logger.warn("Server collection is immutable; cannot remove '{}': {}", serverName, uoe.getMessage());
        } catch (Exception e) {
            logger.warn("Failed to remove server '{}' from server list: {}", serverName, e.getMessage());
        }

        return false;
    }
}
