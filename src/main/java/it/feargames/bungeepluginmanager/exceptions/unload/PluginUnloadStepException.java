package it.feargames.bungeepluginmanager.exceptions.unload;

import lombok.Getter;
import net.md_5.bungee.api.plugin.Plugin;

@Getter
public class PluginUnloadStepException extends PluginUnloadException {

    private final String step;

    public PluginUnloadStepException(final Plugin plugin, final String step, final Throwable cause) {
        super(plugin, "Unable to unload " + plugin.getDescription().getName() + " "
                + plugin.getDescription().getVersion() + ": Step " + step + " failed!", cause);
        this.step = step;
    }
}
