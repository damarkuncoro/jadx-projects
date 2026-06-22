package dexforge.api.plugin;

import dexforge.api.core.DexForgeProject;

/**
 * Context for DexForge plugins to interact with the decompiler engine.
 */
public interface DexForgePluginContext {
	/**
	 * Access to the current project settings and nodes.
	 */
	DexForgeProject getProject();

	/**
	 * Register an extension to add new capabilities.
	 */
	void addExtension(DexForgeExtension extension);

	/**
	 * Register a callback for when the project is loaded.
	 */
	void onProjectLoaded(java.util.function.Consumer<DexForgeProject> callback);
}
