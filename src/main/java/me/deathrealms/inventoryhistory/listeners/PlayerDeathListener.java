package me.deathrealms.inventoryhistory.listeners;

import me.deathrealms.inventoryhistory.InventoryFile;
import me.deathrealms.inventoryhistory.InventoryUtils;
import org.bukkit.GameRule;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.Plugin;

import java.text.SimpleDateFormat;
import java.time.Instant;
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
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd - hh:mm:ss aa");
        inventoryFile.set("date", dateFormat.format(new Date()));
        inventoryFile.set("cause", event.getDeathMessage());
        inventoryFile.set("location", InventoryUtils.objectToString(player.getLocation()));
        inventoryFile.set("restored", "&cFalse");
        inventoryFile.set("contents", InventoryUtils.objectToString(player.getInventory().getContents()));
    }
}
