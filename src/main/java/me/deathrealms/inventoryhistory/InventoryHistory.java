package me.deathrealms.inventoryhistory;

import co.aikar.commands.BukkitCommandManager;
import me.deathrealms.inventoryhistory.commands.GuiCommand;
import me.deathrealms.inventoryhistory.commands.ReloadCommand;
import me.deathrealms.inventoryhistory.listeners.PlayerDeathListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class InventoryHistory extends JavaPlugin {

    @Override
    public void onEnable() {
        File playersDirectory = new File(getDataFolder(), "inventories");
        playersDirectory.mkdirs();
        saveDefaultConfig();

        BukkitCommandManager commandManager = new BukkitCommandManager(this);

        commandManager.getCommandCompletions().registerAsyncCompletion("@allplayers", context -> {
            List<String> players = new ArrayList<>();
            for (String player : playersDirectory.list()) {
                players.add(Bukkit.getOfflinePlayer(UUID.fromString(player)).getName());
            }
            return players;
        });

        commandManager.registerCommand(new GuiCommand(this));
        commandManager.registerCommand(new ReloadCommand(this));

        getServer().getPluginManager().registerEvents(new PlayerDeathListener(this), this);
    }
}
