package it.feargames.bungeepluginmanager.exceptions;

import lombok.Getter;

@Getter
public class PluginNotLoadedException extends Exception {

    private final String name;

    public PluginNotLoadedException(final String name) {
        super("The plugin " + name + " isn't loaded!");
        this.name = name;
    }
}
