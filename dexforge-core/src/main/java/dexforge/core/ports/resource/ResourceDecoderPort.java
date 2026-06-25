package dexforge.core.ports.resource;

import java.util.Optional;

/**
 * Technical port for decoding Android binary XML files.
 */
public interface ResourceDecoderPort {
	Optional<String> decodeBinaryXml(byte[] content);
}
