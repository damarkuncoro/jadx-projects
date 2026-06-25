package dexforge.core.application.resource;

import java.util.Optional;
import dexforge.core.ports.resource.ResourceDecoderPort;

/**
 * Domain-agnostic service for resource processing.
 */
public final class ResourceApplicationService {
	private final ResourceDecoderPort decoderPort;

	public ResourceApplicationService(ResourceDecoderPort decoderPort) {
		this.decoderPort = decoderPort;
	}

	public Optional<String> decodeXml(byte[] content) {
		return decoderPort.decodeBinaryXml(content);
	}
}
