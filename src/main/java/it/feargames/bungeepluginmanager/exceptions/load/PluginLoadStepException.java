package it.feargames.bungeepluginmanager.exceptions.load;

import lombok.Getter;
import net.md_5.bungee.api.plugin.PluginDescription;

import java.io.File;

@Getter
public final class PluginLoadStepException extends PluginLoadException {

    private final PluginDescription plugin;
    private final String step;

    public PluginLoadStepException(final File pluginFile, final PluginDescription plugin, final String step, final Throwable cause) {
        super(pluginFile, "Unable to load " + plugin.getName() + " " + plugin.getVersion()
                + ": Step " + step + " failed!", cause);
        this.plugin = plugin;
        this.step = step;
    }
}
