package dexforge.api.resource;

import java.util.List;
import java.util.Optional;

/**
 * Represents the decoded content of a resource file.
 */
public interface DexForgeResourceContent {
	/**
	 * Name of this content entry.
	 */
	String getName();

	/**
	 * Type of the content (TEXT, BINARY, etc).
	 */
	DexForgeResourceContentType getContentType();

	/**
	 * Get text content if available.
	 */
	Optional<String> getText();

	/**
	 * Get raw binary data if available.
	 */
	Optional<byte[]> getData();

	/**
	 * Get sub-contents (e.g. for resource tables or ZIP-like resources).
	 */
	List<DexForgeResourceContent> getSubContents();
}
