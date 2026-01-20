package fun.ogtimes.skywars.bungee;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import lombok.Getter;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.event.EventHandler;

public class SkyWarsBungee extends Plugin implements Listener {
    @Getter
    public static SkyWarsBungee plugin;
    public static Configuration configuration;
    public static int unknownInt;
    public static String unknownString;
    public static boolean done;

    public void onEnable() {
        plugin = this;

        try {
            ConfigurationProvider provider = ConfigurationProvider.getProvider(YamlConfiguration.class);
            configuration = provider.load(loadResource(this, "config.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!configuration.contains("skywarslobbies_servers")) {
            ArrayList<String> servers = new ArrayList<>();
            servers.add("SkyWarsLobby1");
            servers.add("SkyWarsLobby2");
            configuration.set("skywarslobbies_servers", servers);
            saveFile(this, configuration, "config.yml");
        }

        PluginManager pm = this.getProxy().getPluginManager();
        pm.registerListener(this, this);
        ProxyServer.getInstance().registerChannel("SkyWars-Sign-Send");
        ProxyServer.getInstance().registerChannel("SkyWars-Sign-Update");
    }

    public void sendUpdate(String data) {
        for (String serverName : configuration.getStringList("skywarslobbies_servers")) {
            ServerInfo server = ProxyServer.getInstance().getServerInfo(serverName);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(baos);

            try {
                out.writeUTF(data);
            } catch (IOException e) {
                this.getLogger().severe("An I/O error occurred!");
            }

            server.sendData("SkyWars-Sign-Update", baos.toByteArray());
        }
    }

    @EventHandler
    public void onQueryReceive(PluginMessageEvent event) {
        if (event.getTag().equals("SkyWars-Sign-Send")) {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(event.getData()));

            try {
                String data = in.readUTF();
                this.sendUpdate(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static File loadResource(SkyWarsBungee plugin, String filename) {
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdir();
        }

        File file = new File(dataFolder, filename);

        try {
            if (!file.exists()) {
                file.createNewFile();
                try (InputStream in = plugin.getResourceAsStream(filename);
                     FileOutputStream out = new FileOutputStream(file)) {
                    in.transferTo(out);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return file;
    }

    public static void saveFile(SkyWarsBungee plugin, Configuration config, String filename) {
        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, loadResource(plugin, filename));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}