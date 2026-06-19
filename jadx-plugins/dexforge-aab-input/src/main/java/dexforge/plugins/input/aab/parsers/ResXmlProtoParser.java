package dexforge.plugins.input.aab.parsers;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import jadx.api.ICodeInfo;
import jadx.core.dex.nodes.RootNode;

public class ResXmlProtoParser {
	private final RootNode rootNode;
	private final AaptXmlParser xmlParser;
	private final Map<String, String> tagAttrDeobfNames = new HashMap<>();

	public ResXmlProtoParser(RootNode rootNode) {
		this.rootNode = rootNode;
		this.xmlParser = new AaptXmlParser(new AaptProtoParser());
	}

	public synchronized ICodeInfo parse(InputStream inputStream) throws IOException {
		// 1. Parse to Domain AST
		ProtoXmlElement rootElement = xmlParser.parse(inputStream);

		// 2. Transform (Deobfuscate Pass)
		ProtoXmlDeobfuscator deobfuscator = new ProtoXmlDeobfuscator(rootNode, tagAttrDeobfNames);
		deobfuscator.transform(rootElement);

		// 3. Serialize to XML format
		ProtoXmlWriter writer = new ProtoXmlWriter(rootNode);
		return writer.write(rootElement);
	}
}
