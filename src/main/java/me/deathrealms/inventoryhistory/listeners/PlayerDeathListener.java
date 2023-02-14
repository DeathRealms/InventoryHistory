package me.deathrealms.inventoryhistory.listeners;

import me.deathrealms.inventoryhistory.InventoryFile;
import org.bukkit.GameRule;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

public class PlayerDeathListener implements Listener {
    private final Plugin plugin;

    public PlayerDeathListener(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        if (!player.hasPermission("inventoryhistory.use") || player.getInventory().isEmpty() || player.getWorld().getGameRuleValue(GameRule.KEEP_INVENTORY)) {
            return;
        }

        InventoryFile inventoryFile = new InventoryFile(plugin, player.getUniqueId(), Date.from(Instant.now()).getTime() + ".yml");
        inventoryFile.create();
        inventoryFile.set("date", Date.from(Instant.now()).toString());
        inventoryFile.set("cause", event.getDeathMessage());
        inventoryFile.set("location", objectToString(player.getLocation()));
        inventoryFile.set("restored", "&cFalse");
        inventoryFile.set("contents", objectToString(player.getInventory().getContents()));
    }

    private String objectToString(Object object) {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            BukkitObjectOutputStream data = new BukkitObjectOutputStream(stream);
            data.writeObject(object);
            data.flush();
            return Base64.getEncoder().encodeToString(stream.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
