package it.feargames.bungeepluginmanager;

import com.google.common.collect.Multimap;
import it.feargames.bungeepluginmanager.exceptions.PluginNotLoadedException;
import it.feargames.bungeepluginmanager.exceptions.load.*;
import it.feargames.bungeepluginmanager.exceptions.unload.PluginUnloadClassLoaderException;
import it.feargames.bungeepluginmanager.exceptions.unload.PluginUnloadException;
import it.feargames.bungeepluginmanager.exceptions.unload.PluginUnloadStepException;
import it.feargames.bungeepluginmanager.utils.ConsoleLogger;
import lombok.NonNull;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.*;
import net.md_5.bungee.api.scheduler.TaskScheduler;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Handler;
import java.util.stream.Collectors;

public final class BungeePluginManager extends Plugin {

    private ProxyServer proxy;
    private TaskScheduler scheduler;
    private PluginManager pluginManager;

    private ModifiedPluginEventBus pluginEventBus;

    @Override
    public void onEnable() {
        // Server components
        this.proxy = getProxy();
        this.scheduler = proxy.getScheduler();
        this.pluginManager = proxy.getPluginManager();
        // Prepare logger
        ConsoleLogger.setLogger(getLogger());
        // Inject stuff
        try {
            ConsoleLogger.info("Injecting into PluginManager...");
            pluginEventBus = new ModifiedPluginEventBus(proxy.getLogger());
            ReflectionUtils.setFieldValue(pluginManager, "eventBus", pluginEventBus);
        } catch (Throwable t) {
            ConsoleLogger.exception("Unable to inject into the PluginEventBus!", t);
            return;
        }
        // Register commands
        pluginManager.registerCommand(this, new BungeePluginManagerCommand(this));
    }

    public void unloadPlugin(@NonNull final Plugin plugin) throws PluginUnloadException {
        final PluginDescription description = plugin.getDescription();

        // Get the plugin ClassLoader
        if (!(plugin.getClass().getClassLoader() instanceof PluginClassloader)) {
            throw new PluginUnloadClassLoaderException(plugin);
        }
        final PluginClassloader pluginClassLoader = (PluginClassloader) plugin.getClass().getClassLoader();

        // Call onDisable
        try {
            plugin.onDisable();
        } catch (Throwable t) {
            throw new PluginUnloadStepException(plugin, "invoke onDisable", t);
        }

        // Close all log handlers
        try {
            for (final Handler handler : plugin.getLogger().getHandlers()) {
                handler.close();
            }
        } catch (Throwable t) {
            throw new PluginUnloadStepException(plugin, "unregister log handlers", t);
        }

        // Unregister event handlers
        pluginManager.unregisterListeners(plugin);
        // Unregister commands
        pluginManager.unregisterCommands(plugin);

        // Cancel tasks
        scheduler.cancel(plugin);

        // Shutdown internal executor
        try {
            final ExecutorService executorService = ReflectionUtils.getFieldValue(plugin, "service");
            if (executorService != null) {
                executorService.shutdownNow();
            }
        } catch (Throwable t) {
            throw new PluginUnloadStepException(plugin, "shutdown executor service", t);
        }

        // Stop all still active threads that belong to a plugin
        for (Thread thread : Thread.getAllStackTraces().keySet()) {
            if (thread.getClass().getClassLoader() != pluginClassLoader) {
                continue;
            }
            try {
                thread.interrupt();
                thread.join(2000);
                if (thread.isAlive()) {
                    thread.stop();
                }
            } catch (Throwable t) {
                throw new PluginUnloadStepException(plugin, "stop thread " + thread.getName(), t);
            }
        }

        // Finish uncompleted intents
        pluginEventBus.completeIntents(plugin);

        // Remove commands that were registered by plugin not through normal means
        try {
            final Map<String, Command> commandMap = ReflectionUtils.getFieldValue(pluginManager, "commandMap");
            Objects.requireNonNull(commandMap);
            commandMap.entrySet().removeIf(entry -> entry.getValue().getClass().getClassLoader() == pluginClassLoader);
        } catch (Throwable t) {
            throw new PluginUnloadStepException(plugin, "commandMap cleanup", t);
        }

        // Cleanup internal listener and command maps from plugin refs
        try {
            final Map<String, Plugin> pluginsMap = ReflectionUtils.getFieldValue(pluginManager, "plugins");
            Objects.requireNonNull(pluginsMap);
            pluginsMap.values().remove(plugin);
            final Multimap<Plugin, Command> commands = ReflectionUtils.getFieldValue(pluginManager, "commandsByPlugin");
            Objects.requireNonNull(commands);
            commands.removeAll(plugin);
            final Multimap<Plugin, Listener> listeners = ReflectionUtils.getFieldValue(pluginManager, "listenersByPlugin");
            Objects.requireNonNull(listeners);
            listeners.removeAll(plugin);
        } catch (Throwable t) {
            throw new PluginUnloadStepException(plugin, "BungeeCord internal maps cleanup", t);
        }

        // Close classloader
        try {
            pluginClassLoader.close();
        } catch (Throwable t) {
            throw new PluginUnloadStepException(plugin, "close classloader", t);
        }

        // Remove classloader
        try {
            final Set<PluginClassloader> allLoaders = ReflectionUtils.getStaticFieldValue(PluginClassloader.class, "allLoaders");
            Objects.requireNonNull(allLoaders);
            allLoaders.remove(pluginClassLoader);
        } catch (Throwable t) {
            throw new PluginUnloadStepException(plugin, "remove classloader", t);
        }
    }

    public PluginDescription unloadPlugin(@NonNull final String pluginName) throws PluginNotLoadedException, PluginUnloadException {
        final Plugin plugin = pluginManager.getPlugin(pluginName);
        if (plugin == null) {
            throw new PluginNotLoadedException(pluginName);
        }
        unloadPlugin(plugin);
        return plugin.getDescription();
    }

    public PluginDescription loadPlugin(@NonNull final File pluginFile) throws IllegalArgumentException, PluginLoadException {
        if (!pluginFile.exists()) {
            throw new IllegalArgumentException("The specified file (" + pluginFile + ") must exist!");
        }
        if (!pluginFile.getName().endsWith(".jar")) {
            throw new IllegalArgumentException("The specified file (" + pluginFile + ") must be a jar file!");
        }
        // Load the plugin descriptor
        final PluginDescription description;
        try (final JarFile jar = new JarFile(pluginFile)) {
            // Find the description
            JarEntry descriptionFile = jar.getJarEntry("bungee.yml");
            if (descriptionFile == null) {
                descriptionFile = jar.getJarEntry("plugin.yml");
                if (descriptionFile == null) {
                    throw new PluginLoadDescriptorException(pluginFile);
                }
            }
            // Load descriptor
            try (final InputStream in = jar.getInputStream(descriptionFile)) {
                // Load description
                description = new Yaml().loadAs(in, PluginDescription.class);
                description.setFile(pluginFile);
            }
        } catch (IOException e) {
            throw new PluginLoadIOException(pluginFile, e);
        }
        // Get loaded plugins
        final Set<String> loadedPlugins = pluginManager.getPlugins().stream()
                .map(plugin -> plugin.getDescription().getName())
                .collect(Collectors.toSet());
        // Check if already loaded
        if (loadedPlugins.contains(description.getName())) {
            throw new PluginLoadAlreadyLoadedException(pluginFile, description);
        }
        // Check dependencies
        final Set<String> missingDependencies = description.getDepends().stream()
                .filter(dependency -> !loadedPlugins.contains(dependency))
                .collect(Collectors.toSet());
        if (!missingDependencies.isEmpty()) {
            throw new PluginLoadDependencyException(pluginFile, description, missingDependencies);
        }
        // Init ClassLoader and construct main class
        final Plugin plugin;
        try {
            final PluginClassloader loader = new PluginClassloader(new URL[]{pluginFile.toURI().toURL()});
            final Class<?> mainClass = loader.loadClass(description.getMain());
            plugin = (Plugin) mainClass.getDeclaredConstructor().newInstance();
            // Initialize
            ReflectionUtils.invokeMethod(plugin, "init", proxy, description);
        } catch (Throwable t) {
            throw new PluginLoadStepException(pluginFile, description, "construct main class", t);
        }

        // Register into plugin map
        try {
            Map<String, Plugin> pluginsMap = ReflectionUtils.getFieldValue(pluginManager, "plugins");
            Objects.requireNonNull(pluginsMap);
            pluginsMap.put(description.getName(), plugin);
        } catch (Throwable t) {
            throw new PluginLoadStepException(pluginFile, description, "add to plugin map", t);
        }
        // Invoke onLoad
        try {
            plugin.onLoad();
        } catch (Throwable t) {
            throw new PluginLoadStepException(pluginFile, description, "invoke onLoad", t);
        }
        // Invoke onEnable
        try {
            plugin.onEnable();
        } catch (Throwable t) {
            throw new PluginLoadStepException(pluginFile, description, "invoke onEnable", t);
        }
        return description;
    }

    public PluginDescription loadPlugin(@NonNull final String pluginName) throws IllegalArgumentException, PluginLoadException {
        return loadPlugin(new File(proxy.getPluginsFolder(), pluginName + ".jar"));
    }

    public Collection<Plugin> getPlugins() {
        return pluginManager.getPlugins();
    }
}
