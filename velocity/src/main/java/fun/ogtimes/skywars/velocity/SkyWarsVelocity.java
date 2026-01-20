package fun.ogtimes.skywars.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

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

        logger.info("SkyWars Velocity plugin has been enabled!");
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
}