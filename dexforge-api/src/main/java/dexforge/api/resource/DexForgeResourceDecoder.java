package dexforge.api.resource;

import java.io.InputStream;
import java.util.Optional;

import dexforge.api.engine.DexForgeEngine;

/**
 * Public implementation of IResourceDecoder.
 * SRP: Proxies resource decoding requests to the underlying engine.
 */
public final class DexForgeResourceDecoder implements IResourceDecoder {
	private final DexForgeEngine engine;

	public DexForgeResourceDecoder(DexForgeEngine engine) {
		this.engine = engine;
	}

	@Override
	public Optional<String> decodeXml(InputStream inputStream) {
		try {
			byte[] bytes = inputStream.readAllBytes();
			return decodeXml(bytes);
		} catch (Exception e) {
			return Optional.empty();
		}
	}

	@Override
	public Optional<String> decodeXml(byte[] bytes) {
		// We should add this to DexForgeEngine to avoid dependence on the full project load
		// but for now we can use getResourceText if we have a raw resource.
		// Let's add decodeBinaryXml to DexForgeEngine.
		return Optional.ofNullable(engine.decodeBinaryXml(bytes));
	}
}
