package it.feargames.bungeepluginmanager.exceptions.unload;

import lombok.Getter;
import net.md_5.bungee.api.plugin.Plugin;

@Getter
public class PluginUnloadClassLoaderException extends PluginUnloadException {

    public PluginUnloadClassLoaderException(final Plugin plugin) {
        super(plugin, "Unable to unload " + plugin.getDescription().getName() + " "
                + plugin.getDescription().getVersion() + ": The ClassLoader must be a valid PluginClassloader!");
    }
}
