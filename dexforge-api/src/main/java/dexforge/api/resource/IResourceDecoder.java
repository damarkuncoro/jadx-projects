package dexforge.api.resource;

import java.io.InputStream;
import java.util.Optional;

/**
 * Interface for decoding binary Android resources (like AndroidManifest.xml) into human-readable text.
 */
public interface IResourceDecoder {

	/**
	 * Decode a binary XML stream.
	 */
	Optional<String> decodeXml(InputStream inputStream);

	/**
	 * Decode a binary XML from byte array.
	 */
	Optional<String> decodeXml(byte[] bytes);
}
