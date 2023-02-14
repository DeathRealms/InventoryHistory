package me.deathrealms.inventoryhistory;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class InventoryFile {
    private final Plugin plugin;
    private final File file;
    private FileConfiguration config;

    public InventoryFile(Plugin plugin, UUID uuid, String fileName) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder() + File.separator + "inventories", uuid + File.separator + fileName);
        this.config = YamlConfiguration.loadConfiguration(this.file);
    }

    public void create() {
        if (file.exists()) {
            return;
        }

        this.file.getParentFile().mkdirs();
        save();
        reload();
    }

    public void reload() {
        this.config = YamlConfiguration.loadConfiguration(this.file);
    }

    public void save() {
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            try {
                this.config.save(this.file);
            } catch (IOException e) {
                Bukkit.getLogger().severe(e.getMessage());
            }
        });
    }

    public void delete() {
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> this.file.delete());
    }

    public void set(String path, Object value) {
        this.config.set(path, value);
        save();
    }

    public FileConfiguration getConfig() {
        return this.config;
    }
}
