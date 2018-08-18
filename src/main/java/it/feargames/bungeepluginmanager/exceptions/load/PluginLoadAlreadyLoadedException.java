package it.feargames.bungeepluginmanager.exceptions.load;

import lombok.Getter;
import net.md_5.bungee.api.plugin.PluginDescription;

import java.io.File;

@Getter
public final class PluginLoadAlreadyLoadedException extends PluginLoadException {

    private final PluginDescription plugin;

    public PluginLoadAlreadyLoadedException(final File pluginFile, final PluginDescription plugin) {
        super(pluginFile, "Unable to load " + plugin.getName() + " " + plugin.getVersion()
                + ": Already loaded!");
        this.plugin = plugin;
    }
}
