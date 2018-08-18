package it.feargames.bungeepluginmanager.exceptions.load;

import lombok.Getter;
import net.md_5.bungee.api.plugin.PluginDescription;

import java.io.File;
import java.util.Set;

@Getter
public final class PluginLoadDependencyException extends PluginLoadException {

    private final PluginDescription plugin;
    private final Set<String> missingDependencies;

    public PluginLoadDependencyException(final File pluginFile, final PluginDescription plugin, final Set<String> missingDependencies) {
        super(pluginFile, "Unable to load " + plugin.getName() + " " + plugin.getVersion()
                + ": Missing dependencies: " + String.join(", ", missingDependencies));
        this.plugin = plugin;
        this.missingDependencies = missingDependencies;
    }
}
