package dexforge.api.plugin;

/**
 * Base interface for all DexForge plugins.
 */
public interface DexForgePlugin {
	/**
	 * Get metadata about this plugin.
	 */
	DexForgePluginInfo getInfo();

	/**
	 * Initialize the plugin with the provided context.
	 */
	void init(DexForgePluginContext context);

	/**
	 * Unload the plugin and release resources.
	 */
	default void unload() {
	}
}
