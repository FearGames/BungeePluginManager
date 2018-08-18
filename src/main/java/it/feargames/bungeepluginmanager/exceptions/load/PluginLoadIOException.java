package it.feargames.bungeepluginmanager.exceptions.load;

import lombok.Getter;

import java.io.File;
import java.io.IOException;

public final class PluginLoadIOException extends PluginLoadException {

    public PluginLoadIOException(final File pluginFile, final IOException cause) {
        super(pluginFile, "Unable to load " + pluginFile.getName() + ": IOException", cause);
    }
}
