package dexforge.api.plugin.type;

import dexforge.api.model.DexForgeClass;
import dexforge.api.plugin.DexForgeExtension;

/**
 * Extension point for analyzing or modifying a class after decompilation.
 */
public interface DexForgeCodeProcessor extends DexForgeExtension {
	/**
	 * Process the decompiled class.
	 * Can be used for vulnerability scanning, API mapping, etc.
	 */
	void process(DexForgeClass cls);
}
