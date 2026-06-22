package dexforge.api;

import dexforge.api.plugins.JadxPlugin;

/**
 * DexForge plugin marker for new plugins.
 * <p>
 * The current runtime still adapts to the existing plugin contract, so this interface extends
 * the compatibility type until the plugin runtime is fully DexForge-owned.
 */
public interface DexForgePlugin extends JadxPlugin {
}
