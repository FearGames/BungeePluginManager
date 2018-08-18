package it.feargames.bungeepluginmanager;

import it.feargames.bungeepluginmanager.exceptions.PluginNotLoadedException;
import it.feargames.bungeepluginmanager.exceptions.load.PluginLoadException;
import it.feargames.bungeepluginmanager.exceptions.unload.PluginUnloadException;
import lombok.NonNull;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginDescription;

import java.util.Collection;

import static net.md_5.bungee.api.ChatColor.*;

public final class BungeePluginManagerCommand extends Command {

    private final BungeePluginManager pluginManager;

    public BungeePluginManagerCommand(@NonNull final BungeePluginManager pluginManager) {
        super("bpm", "bpm.commands", "pluginmanager", "pm", "plugin");
        this.pluginManager = pluginManager;
    }

    @Override
    public void execute(final CommandSender sender, final String[] args) {
        if (args.length < 1) {
            sendHelp(sender);
            return;
        }

        // 1 argument commands
        switch (args[0].toLowerCase()) {
            case "help":
                sendHelp(sender);
                return;
            case "list": {
                final Collection<Plugin> plugins = pluginManager.getPlugins();
                final ComponentBuilder builder = new ComponentBuilder("Plugins[" + plugins.size() + "]: ");
                plugins.forEach(plugin -> builder.append(plugin.getDescription().getName()).color(GREEN).append(", ").color(WHITE));
                sender.sendMessage(builder.create());
                return;
            }
        }

        if (args.length < 2) {
            sender.sendMessage(textWithColor("Command not found. Type /bpm help to see available commands.", RED));
            return;
        }

        // 2 arguments commands
        final String argument = args[1];
        switch (args[0].toLowerCase()) {
            case "unload": {
                try {
                    final PluginDescription unloadedDescription = pluginManager.unloadPlugin(argument);
                    sender.sendMessage(textWithColor("Unloaded " + unloadedDescription.getName() + " " + unloadedDescription.getVersion(), YELLOW));
                } catch (PluginNotLoadedException | PluginUnloadException e) {
                    sender.sendMessage(textWithColor(e.getMessage(), RED));
                }
                return;
            }
            case "load": {
                sender.sendMessage(textWithColor("Reloading " + argument + "...", YELLOW));
                try {
                    final PluginDescription loadedDescription = pluginManager.loadPlugin(argument);
                    sender.sendMessage(textWithColor("Loaded " + loadedDescription.getName() + " " + loadedDescription.getVersion(), GREEN));
                } catch (PluginLoadException e) {
                    sender.sendMessage(textWithColor(e.getMessage(), RED));
                }
                return;
            }
            case "reload": {
                sender.sendMessage(textWithColor("Reloading " + argument + "...", GOLD));
                try {
                    final PluginDescription unloadedDescription = pluginManager.unloadPlugin(argument);
                    sender.sendMessage(textWithColor("Unloaded " + unloadedDescription.getName() + " " + unloadedDescription.getVersion(), YELLOW));
                    final PluginDescription loadedDescription = pluginManager.loadPlugin(argument);
                    sender.sendMessage(textWithColor("Loaded " + loadedDescription.getName() + " " + loadedDescription.getVersion(), GREEN));
                } catch (PluginNotLoadedException | PluginUnloadException | PluginLoadException e) {
                    sender.sendMessage(textWithColor(e.getMessage(), RED));
                }
                return;
            }
        }

        sender.sendMessage(textWithColor("Command not found. Type /bpm help to see available commands.", RED));
    }

    private static TextComponent textWithColor(@NonNull final String message, final @NonNull ChatColor color) {
        TextComponent text = new TextComponent(message);
        text.setColor(color);
        return text;
    }

    private static void sendHelp(@NonNull final CommandSender sender) {
        sender.sendMessage(translateAlternateColorCodes('&', "&6&l---- BungeePluginManager ----"));
        sender.sendMessage(translateAlternateColorCodes('&', ""));
        sender.sendMessage(translateAlternateColorCodes('&', "&6/bpm help: &fDisplay this message"));
        sender.sendMessage(translateAlternateColorCodes('&', "&6/bpm load &a<plugin>: &fLoads a plugin"));
        sender.sendMessage(translateAlternateColorCodes('&', "&6/bpm unload &a<plugin>: &fUnloads a plugin"));
        sender.sendMessage(translateAlternateColorCodes('&', "&6/bpm reload &a<plugin>: &fReloads a plugin"));
        sender.sendMessage(translateAlternateColorCodes('&', "&6/bpm list: &fList all plugins on the bungee"));
    }
}
