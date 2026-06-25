package dexforge.api.model;

import java.util.List;

/**
 * Interface for scheduling decompilation tasks in batches.
 * Useful for parallel processing and priority-based decompilation.
 */
public interface IDexForgeDecompileScheduler {

	/**
	 * Organizes a list of classes into batches for processing.
	 */
	List<List<DexForgeClass>> buildBatches(List<DexForgeClass> classes);
}
