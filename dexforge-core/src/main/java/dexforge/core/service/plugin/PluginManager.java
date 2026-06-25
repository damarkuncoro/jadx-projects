package dexforge.core.service.plugin;

import dexforge.api.plugin.IDexForgePlugin;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REUSEABLE: Central registry and manager for DexForge plugins.
 * Handles enabling/disabling plugins at runtime.
 */
public final class PluginManager {
    private final Map<String, IDexForgePlugin> registeredPlugins = new HashMap<>();
    private final Map<String, Boolean> pluginStatus = new HashMap<>();

    public void registerPlugin(IDexForgePlugin plugin) {
        String name = plugin.getName();
        registeredPlugins.put(name, plugin);
        pluginStatus.put(name, true); // Enabled by default
        plugin.onInitialize(new HashMap<>());
    }

    public void setEnabled(String pluginName, boolean enabled) {
        if (pluginStatus.containsKey(pluginName)) {
            pluginStatus.put(pluginName, enabled);
        }
    }

    public boolean isEnabled(String pluginName) {
        return pluginStatus.getOrDefault(pluginName, false);
    }

    public List<IDexForgePlugin> getPlugins() {
        return new ArrayList<>(registeredPlugins.values());
    }

    public <T extends IDexForgePlugin> T getPlugin(Class<T> type) {
        for (IDexForgePlugin p : registeredPlugins.values()) {
            if (type.isInstance(p)) return type.cast(p);
        }
        return null;
    }
}
