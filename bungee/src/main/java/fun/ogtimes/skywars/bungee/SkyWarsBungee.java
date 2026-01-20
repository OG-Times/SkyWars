package fun.ogtimes.skywars.bungee;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.event.EventHandler;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SkyWarsBungee extends Plugin implements Listener {

    private Configuration configuration;

    @Override
    public void onEnable() {
        loadConfig();

        ProxyServer.getInstance().getPluginManager().registerListener(this, this);
        ProxyServer.getInstance().registerChannel("skywars:sign-send");
        ProxyServer.getInstance().registerChannel("skywars:sign-update");

        getLogger().severe("Bungeecord is highly deprecated and will not receive updates or support in the future on this fork. Please switch to Velocity.");
        getLogger().info("SkyWars Bungee plugin enabled");
    }

    @EventHandler
    public void onPluginMessage(PluginMessageEvent event) {
        if (event.getSender() == null || !event.getTag().equals("skywars:sign-send")) {
            return;
        }

        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(event.getData()))) {
            String data = in.readUTF();
            sendUpdate(data);
        } catch (IOException e) {
            getLogger().severe("Failed to read plugin message");
            e.printStackTrace();
        }
    }

    private void sendUpdate(String data) {
        List<String> servers = configuration.getStringList("skywarslobbies_servers");

        for (String serverName : servers) {
            ServerInfo server = ProxyServer.getInstance().getServerInfo(serverName);

            if (server == null) {
                getLogger().warning("Server '" + serverName + "' not found in proxy config");
                continue;
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (DataOutputStream out = new DataOutputStream(baos)) {
                out.writeUTF(data);
            } catch (IOException e) {
                getLogger().severe("Failed to write plugin message for " + serverName);
                continue;
            }

            server.sendData("skywars:sign-update", baos.toByteArray());
        }
    }

    private void loadConfig() {
        try {
            if (!getDataFolder().exists()) {
                getDataFolder().mkdirs();
            }

            File file = new File(getDataFolder(), "config.yml");
            if (!file.exists()) {
                file.createNewFile();

                Configuration defaultConfig = new Configuration();
                List<String> servers = new ArrayList<>();
                servers.add("SkyWarsLobby1");
                servers.add("SkyWarsLobby2");
                defaultConfig.set("skywarslobbies_servers", servers);

                ConfigurationProvider.getProvider(YamlConfiguration.class)
                        .save(defaultConfig, file);
            }

            configuration = ConfigurationProvider.getProvider(YamlConfiguration.class)
                    .load(file);

        } catch (IOException e) {
            getLogger().severe("Failed to load config.yml");
            e.printStackTrace();
        }
    }
}
