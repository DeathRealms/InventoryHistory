package me.deathrealms.inventoryhistory.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Optional;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import me.deathrealms.inventoryhistory.InventoryFile;
import me.deathrealms.inventoryhistory.InventoryItem;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.io.BukkitObjectInputStream;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Base64;
import java.util.UUID;

@CommandAlias("inventoryhistory|invhist|invhistory|inventoryhist|ih")
@CommandPermission("inventoryhistory.command.gui")
public final class GuiCommand extends BaseCommand {
    private final Plugin plugin;
    private final File playersDirectory;

    public GuiCommand(Plugin plugin) {
        this.plugin = plugin;
        this.playersDirectory = new File(plugin.getDataFolder(), "inventories");
    }

    @Default
    @CommandCompletion("@allplayers")
    public void onCommand(Player player, @Optional OfflinePlayer other) {
        if (other != null) {
            createInventoryHistoryGui(player, other);
            return;
        }
        createPlayersGui(player);
    }

    private void createPlayersGui(Player player) {
        PaginatedGui playersGui = Gui.paginated().title(Component.text("Players")).rows(6).disableAllInteractions().create();

        for (String playerUUID : playersDirectory.list()) {
            OfflinePlayer other = Bukkit.getOfflinePlayer(UUID.fromString(playerUUID));
            InventoryItem playerHeadItem = new InventoryItem(Material.PLAYER_HEAD);
            playerHeadItem.setDisplayName("&e" + Bukkit.getOfflinePlayer(UUID.fromString(playerUUID)).getName());
            playerHeadItem.setAction(action -> createInventoryHistoryGui(player, other));
            playersGui.addItem(playerHeadItem.build());
        }

        playersGui.setItem(49, new InventoryItem(Material.BARRIER, action -> player.closeInventory()).setDisplayName("&cClose").build());

        playersGui.open(player);
    }

    private void createInventoryHistoryGui(Player player, OfflinePlayer other) {
        PaginatedGui inventoryHistoryGui = Gui.paginated().title(Component.text("Inventory History")).rows(6).disableAllInteractions().create();

        File inventoriesDirectory = new File(playersDirectory, other.getUniqueId().toString());
        for (String inventoryFilename : inventoriesDirectory.list()) {
            InventoryFile inventoryFile = new InventoryFile(plugin, other.getUniqueId(), inventoryFilename);

            InventoryItem bookItem = new InventoryItem(Material.BOOK);
            bookItem.setDisplayName("&e" + inventoryFile.getConfig().getString("date"));
            bookItem.addLore("&eCause: &a" + inventoryFile.getConfig().getString("cause"));
            bookItem.addLore("&eRestored: " + inventoryFile.getConfig().getString("restored"));
            bookItem.setAction(action -> createInventoryGui(player, inventoryFile, other));
            inventoryHistoryGui.addItem(bookItem.build());
        }

        inventoryHistoryGui.setItem(45, new InventoryItem(Material.BARRIER, action -> {
            for (File file : inventoriesDirectory.listFiles()) {
                file.delete();
            }
            Bukkit.getScheduler().runTaskLater(plugin, () -> createInventoryHistoryGui(player, other), 2);
        }).setDisplayName("&cDelete All Inventories").build());

        inventoryHistoryGui.setItem(49, new InventoryItem(Material.BOOK, action -> createPlayersGui(player)).setDisplayName("&cBack").build());

        inventoryHistoryGui.open(player);
    }

    private void createInventoryGui(Player player, InventoryFile inventoryFile, OfflinePlayer other) {
        Gui inventoryGui = Gui.gui().title(Component.text("Inventory")).rows(6).disableAllInteractions().create();
        ItemStack[] contents = (ItemStack[]) stringToObject(inventoryFile.getConfig().getString("contents"));
        // Need to create a copy because the Gui API adds NBT tags to the items.
        ItemStack[] contentsCopy = (ItemStack[]) stringToObject(inventoryFile.getConfig().getString("contents"));

        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            ItemStack itemCopy = contentsCopy[i];
            if (item == null) {
                inventoryGui.addItem(new GuiItem(Material.AIR));
            } else {
                InventoryItem contentItem = new InventoryItem(item);
                contentItem.addLore("&aClick to restore this item to the player.");
                contentItem.setAction(action -> {
                    if (!other.isOnline()) {
                        return;
                    }

                    if (!other.getPlayer().getInventory().addItem(itemCopy).isEmpty()) {
                        other.getPlayer().getLocation().getWorld().dropItemNaturally(other.getPlayer().getLocation(), itemCopy);
                    }
                });
                inventoryGui.addItem(contentItem.build());
            }
        }

        inventoryGui.setItem(45, new InventoryItem(Material.BARRIER, action -> {
            inventoryFile.delete();
            Bukkit.getScheduler().runTaskLater(plugin, () -> createInventoryHistoryGui(player, other), 2);
        }).setDisplayName("&cDelete Inventory").build());

        inventoryGui.setItem(49, new InventoryItem(Material.BOOK, action -> createInventoryHistoryGui(player, other)).setDisplayName("&cBack").build());

        InventoryItem teleportItem = new InventoryItem(Material.COMPASS, action -> {
            if (!other.isOnline()) {
                return;
            }

            other.getPlayer().teleport((Location) stringToObject(inventoryFile.getConfig().getString("location")));

            player.closeInventory();
        });
        teleportItem.setDisplayName("&aTeleport to death location");
        inventoryGui.setItem(52, teleportItem.build());

        InventoryItem restoreItem = new InventoryItem(Material.CHEST, action -> {
            if (!other.isOnline()) {
                return;
            }

            if (plugin.getConfig().getBoolean("overwrite-inventory")) {
                other.getPlayer().getInventory().setContents(contentsCopy);
            } else {
                for (ItemStack item : contentsCopy) {
                    if (item == null) continue;
                    if (!other.getPlayer().getInventory().addItem(item).isEmpty()) {
                        other.getPlayer().getLocation().getWorld().dropItemNaturally(other.getPlayer().getLocation(), item);
                    }
                }
            }

            if (plugin.getConfig().getBoolean("delete-after-restore")) {
                inventoryFile.delete();
            } else {
                inventoryFile.set("restored", "&aTrue");
            }

            player.closeInventory();
        });
        restoreItem.setDisplayName("&aRestore Inventory");
        inventoryGui.setItem(53, restoreItem.build());

        inventoryGui.open(player);
    }

    private Object stringToObject(String contentsData) {
        try {
            ByteArrayInputStream stream = new ByteArrayInputStream(Base64.getDecoder().decode(contentsData));
            BukkitObjectInputStream data = new BukkitObjectInputStream(stream);
            return data.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}