package br.com.azalim.bungeepluginmanager;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import net.md_5.bungee.api.event.AsyncEvent;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventBus;

public class ModifiedPluginEventBus extends EventBus {

    private static final Set<AsyncEvent<?>> UNCOMPLETED_EVENTS = Collections.newSetFromMap(new WeakHashMap<AsyncEvent<?>, Boolean>());
    private static final Object LOCK = new Object();

    public static void completeIntents(Plugin plugin) {
        synchronized (LOCK) {
            UNCOMPLETED_EVENTS.stream().forEach(event -> {
                try {
                    event.completeIntent(plugin);
                } catch (Throwable t) {
                }
            });
        }
    }

    @Override
    public void post(Object event) {
        if (event instanceof AsyncEvent) {
            synchronized (LOCK) {
                UNCOMPLETED_EVENTS.add((AsyncEvent<?>) event);
            }
        }
        super.post(event);
    }

}
