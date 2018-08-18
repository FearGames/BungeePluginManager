package it.feargames.bungeepluginmanager.exceptions.unload;

import lombok.Getter;
import net.md_5.bungee.api.plugin.Plugin;

@Getter
public abstract class PluginUnloadException extends Exception {

    private final Plugin plugin;

    public PluginUnloadException(final Plugin plugin, final String message) {
        super(message);
        this.plugin = plugin;
    }

    public PluginUnloadException(final Plugin plugin, final String message, final Throwable cause) {
        super(message, cause);
        this.plugin = plugin;
    }
}
