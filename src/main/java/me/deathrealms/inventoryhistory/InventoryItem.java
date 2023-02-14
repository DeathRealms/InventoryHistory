package me.deathrealms.inventoryhistory;

import dev.triumphteam.gui.components.GuiAction;
import dev.triumphteam.gui.guis.GuiItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class InventoryItem {
    private final ItemStack item;
    private final ItemMeta meta;
    private GuiAction<InventoryClickEvent> action;

    public InventoryItem(Material material) {
        this(new ItemStack(material), null);
    }

    public InventoryItem(Material material, GuiAction<InventoryClickEvent> action) {
        this(new ItemStack(material), action);
    }

    public InventoryItem(ItemStack item) {
        this(item, null);
    }

    public InventoryItem(ItemStack item, GuiAction<InventoryClickEvent> action) {
        this.item = item;
        this.meta = item.getItemMeta();
        this.action = action;
    }

    public InventoryItem setSkullOwner(String owner) {
        SkullMeta meta = ((SkullMeta) this.meta);
        meta.setOwner(owner);
        return this;
    }

    public InventoryItem setDisplayName(String name) {
        meta.setDisplayName(color(name));
        return this;
    }

    public InventoryItem addLore(String... lines) {
        List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
        for (String line : lines) {
            lore.add(color(line));
        }
        meta.setLore(lore);
        return this;
    }

    public InventoryItem setLore(String... lines) {
        return setLore(Arrays.asList(lines));
    }

    public InventoryItem setLore(List<String> lines) {
        List<String> lore = new ArrayList<>();
        meta.setLore(lore);
        for (String line : lines) {
            lore.add(color(line));
        }
        meta.setLore(lore);
        return this;
    }

    public InventoryItem setAction(GuiAction<InventoryClickEvent> action) {
        this.action = action;
        return this;
    }

    public GuiItem build() {
        item.setItemMeta(meta);
        return new GuiItem(item, action);
    }

    private String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
