package dexforge.plugins.input.aab.parsers;

import java.util.Map;
import java.util.Random;

import jadx.core.dex.nodes.RootNode;
import jadx.core.xmlgen.XMLChar;
import jadx.core.xmlgen.XmlDeobf;

public class ProtoXmlDeobfuscator {
	private final RootNode rootNode;
	private final Map<String, String> tagAttrDeobfNames;
	private String appPackageName;

	public ProtoXmlDeobfuscator(RootNode rootNode, Map<String, String> tagAttrDeobfNames) {
		this.rootNode = rootNode;
		this.tagAttrDeobfNames = tagAttrDeobfNames;
	}

	public void transform(ProtoXmlElement rootElement) {
		appPackageName = null;
		processElement(rootElement);
	}

	private void processElement(ProtoXmlElement e) {
		// Memorize package name from manifest
		if ("manifest".equals(e.getName())) {
			for (ProtoXmlAttribute attr : e.getAttributes()) {
				if ("package".equals(attr.getName())) {
					appPackageName = attr.getValue();
					break;
				}
			}
		}

		// Deobfuscate element name
		String tag = deobfClassName(e.getName());
		tag = getValidTagAttributeName(tag);
		e.setName(tag);

		// Deobfuscate attributes
		for (ProtoXmlAttribute attr : e.getAttributes()) {
			String val = deobfClassName(attr.getValue());
			attr.setValue(val);
		}

		// Recursively process children
		for (ProtoXmlNode child : e.getChildren()) {
			if (child instanceof ProtoXmlElement) {
				processElement((ProtoXmlElement) child);
			}
		}
	}

	private String deobfClassName(String className) {
		String newName = XmlDeobf.deobfClassName(rootNode, className, appPackageName);
		if (newName != null) {
			return newName;
		}
		return className;
	}

	private String getValidTagAttributeName(String originalName) {
		if (XMLChar.isValidName(originalName)) {
			return originalName;
		}
		if (tagAttrDeobfNames.containsKey(originalName)) {
			return tagAttrDeobfNames.get(originalName);
		}
		String generated;
		do {
			generated = generateTagAttrName();
		} while (tagAttrDeobfNames.containsValue(generated));
		tagAttrDeobfNames.put(originalName, generated);
		return generated;
	}

	private static String generateTagAttrName() {
		final int length = 6;
		Random r = new Random();
		StringBuilder sb = new StringBuilder();
		for (int i = 1; i <= length; i++) {
			sb.append((char) (r.nextInt(26) + 'a'));
		}
		return sb.toString();
	}
}
