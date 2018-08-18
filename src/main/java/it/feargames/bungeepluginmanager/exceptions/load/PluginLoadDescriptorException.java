package it.feargames.bungeepluginmanager.exceptions.load;

import java.io.File;

public final class PluginLoadDescriptorException extends PluginLoadException {

    public PluginLoadDescriptorException(final File pluginFile) {
        super(pluginFile, "Unable to load " + pluginFile + ": Missing descriptor file!");
    }
}
