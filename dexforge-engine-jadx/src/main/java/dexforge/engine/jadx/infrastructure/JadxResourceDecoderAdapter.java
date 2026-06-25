package dexforge.engine.jadx.infrastructure;

import java.io.ByteArrayInputStream;
import java.util.Optional;

import dexforge.core.ports.resource.ResourceDecoderPort;
import jadx.api.JadxDecompiler;
import jadx.core.dex.nodes.RootNode;
import jadx.core.xmlgen.BinaryXMLParser;

/**
 * JADX implementation of ResourceDecoderPort.
 */
public final class JadxResourceDecoderAdapter implements ResourceDecoderPort {
	private final JadxDecompiler decompiler;

	public JadxResourceDecoderAdapter(JadxDecompiler decompiler) {
		this.decompiler = decompiler;
	}

	@Override
	public Optional<String> decodeBinaryXml(byte[] content) {
		try {
			RootNode root = decompiler.getRoot();
			BinaryXMLParser parser = new BinaryXMLParser(root);
			String xml = parser.parse(new ByteArrayInputStream(content)).getCodeStr();
			return Optional.of(xml);
		} catch (Exception e) {
			return Optional.empty();
		}
	}
}
