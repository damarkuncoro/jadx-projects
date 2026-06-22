package dexforge.api.plugin;

/**
 * Base interface for all DexForge extension points.
 */
public interface DexForgeExtension {
	/**
	 * Unique ID for this specific extension instance.
	 */
	String getExtensionId();
}
