package me.deathrealms.inventoryhistory.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Subcommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

@CommandAlias("inventoryhistory|invhist|invhistory|inventoryhist|ih")
public final class ReloadCommand extends BaseCommand {
    private final Plugin plugin;

    public ReloadCommand(Plugin plugin) {
        this.plugin = plugin;
    }

    @Subcommand("reload")
    @CommandPermission("inventoryhistory.command.reload")
    public void reload(CommandSender sender) {
        plugin.reloadConfig();
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7[&aInventoryHistory&7] The configuration was reloaded!"));
    }
}
