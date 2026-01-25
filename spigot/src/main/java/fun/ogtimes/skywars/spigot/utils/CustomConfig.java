package fun.ogtimes.skywars.spigot.utils;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class CustomConfig {
    JavaPlugin plugin;
    private String name;
    private File file;
    private FileConfiguration fileConfig;

    public CustomConfig(String name) {
        this.name = name;
    }

    public CustomConfig(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public FileConfiguration getCustomConfig(CustomConfig config) {
        if (config.fileConfig == null) {
            this.reloadCustomConfig(config);
        }

        return config.fileConfig;
    }

    public void reloadCustomConfig(CustomConfig config) {
        if (config.fileConfig == null) {
            config.file = new File(this.plugin.getDataFolder(), config.name + ".properties");
        }

        config.fileConfig = YamlConfiguration.loadConfiguration(config.file);
        if (config.fileConfig != null) {
            YamlConfiguration var2 = YamlConfiguration.loadConfiguration(config.file);
            config.fileConfig.setDefaults(var2);
        }

    }

    public void saveCustomConfig(CustomConfig config) {
        if (config.fileConfig != null && config.file != null) {
            try {
                this.getCustomConfig(config).save(config.file);
            } catch (IOException ex) {
                this.plugin.getLogger().log(Level.SEVERE, "Could not save config to " + config.file, ex);
            }

        }
    }

    public void saveDefaultConfig(CustomConfig config) {
        if (config.file == null) {
            config.file = new File(this.plugin.getDataFolder(), config.name + ".properties");
        }

        if (!config.file.exists()) {
            this.plugin.saveResource(config.name + ".properties", false);
        }

    }
}
