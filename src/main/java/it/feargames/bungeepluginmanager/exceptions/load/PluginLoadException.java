package it.feargames.bungeepluginmanager.exceptions.load;

import lombok.Getter;

import java.io.File;

@Getter
public abstract class PluginLoadException extends Exception {

    private final File pluginFile;

    public PluginLoadException(final File pluginFile, final String message) {
        super(message);
        this.pluginFile = pluginFile;
    }

    public PluginLoadException(final File pluginFile, final String message, final Throwable cause) {
        super(message, cause);
        this.pluginFile = pluginFile;
    }
}
