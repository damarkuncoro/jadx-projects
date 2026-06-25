package dexforge.api.model;

import java.io.Closeable;
import java.util.Optional;

/**
 * Enhanced caching interface for decompiled code information.
 * Allows consumers to provide their own caching logic (Disk, Memory, LIRS, etc.)
 */
public interface IDexForgeCodeCache extends Closeable {

	/**
	 * Store code info for a class.
	 */
	void add(String classFullName, ICodeInfo codeInfo);

	/**
	 * Remove a class from the cache.
	 */
	void remove(String classFullName);

	/**
	 * Get code info from cache.
	 */
	Optional<ICodeInfo> get(String classFullName);

	/**
	 * Check if the class exists in cache.
	 */
	boolean contains(String classFullName);

	/**
	 * Clear all cached entries.
	 */
	void clear();

	/**
	 * Get cache usage statistics (size, hits, etc.)
	 */
	default String getStats() {
		return "Statistics not available";
	}
}
