package dexforge.api.plugin;

import java.util.Map;

/**
 * Interface for all DexForge plugins.
 * Defines lifecycle and metadata for pluggable components.
 */
public interface IDexForgePlugin {
    String getName();
    String getDescription();
    String getVersion();

    /**
     * Called when the plugin is loaded into the system.
     */
    void onInitialize(Map<String, Object> context);

    /**
     * Called when the plugin is removed.
     */
    void onShutdown();
}
