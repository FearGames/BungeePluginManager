package it.feargames.bungeepluginmanager;

import lombok.NonNull;
import net.md_5.bungee.api.event.AsyncEvent;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventBus;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Logger;

public class ModifiedPluginEventBus extends EventBus {

    private final Set<AsyncEvent<?>> uncompletedEvents;
    private final Object lock;

    public ModifiedPluginEventBus(final Logger logger) {
        super(logger);
        uncompletedEvents = Collections.newSetFromMap(new WeakHashMap<>());
        lock = new Object();
    }

    public void completeIntents(@NonNull final Plugin plugin) {
        synchronized (lock) {
            uncompletedEvents.forEach(event -> {
                try {
                    event.completeIntent(plugin);
                } catch (Throwable ignored) {
                }
            });
        }
    }

    @Override
    public void post(Object event) {
        if (event instanceof AsyncEvent) {
            synchronized (lock) {
                uncompletedEvents.add((AsyncEvent<?>) event);
            }
        }
        super.post(event);
    }
}
