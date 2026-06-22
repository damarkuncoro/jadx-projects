package dexforge.plugins.input.aab.parsers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jadx.api.ICodeInfo;
import jadx.api.ICodeWriter;
import jadx.core.dex.nodes.RootNode;
import jadx.core.utils.StringUtils;
import jadx.core.utils.android.AndroidResourcesMap;
import jadx.core.xmlgen.XmlDeobf;

public class ProtoXmlWriter {
	private static final String ANDROID_NS_URL = "http://schemas.android.com/apk/res/android";

	private final RootNode rootNode;
	private final boolean isPrettyPrint;

	public ProtoXmlWriter(RootNode rootNode) {
		this.rootNode = rootNode;
		this.isPrettyPrint = !rootNode.getArgs().isSkipXmlPrettyPrint();
	}

	public ICodeInfo write(ProtoXmlElement rootElement) {
		ICodeWriter writer = rootNode.makeCodeWriter();
		writer.add("<?xml version=\"1.0\" encoding=\"utf-8\"?>");

		Map<String, String> nsMap = new HashMap<>();
		writeNode(rootElement, writer, nsMap);

		return writer.finish();
	}

	private void writeNode(ProtoXmlNode node, ICodeWriter writer, Map<String, String> nsMap) {
		if (node.getSourceLine() > 0) {
			writer.attachSourceLine(node.getSourceLine());
		}
		if (node instanceof ProtoXmlTextNode) {
			writer.add(StringUtils.escapeXML(((ProtoXmlTextNode) node).getText().trim()));
		} else if (node instanceof ProtoXmlElement) {
			writeElement((ProtoXmlElement) node, writer, nsMap);
		}
	}

	private void writeElement(ProtoXmlElement e, ICodeWriter writer, Map<String, String> nsMap) {
		writer.startLine('<').add(e.getName());

		// Write namespaces
		int nsCount = e.getNamespaces().size();
		boolean newLine = nsCount != 1 && isPrettyPrint;
		if (nsCount > 0) {
			writer.add(' ');
		}
		for (int i = 0; i < nsCount; i++) {
			ProtoXmlNamespace ns = e.getNamespaces().get(i);
			nsMap.put(ns.getUri(), ns.getPrefix());
			writer.add("xmlns:").add(ns.getPrefix()).add("=\"").add(ns.getUri()).add('"');
			if (i < nsCount - 1) {
				if (newLine) {
					writer.startLine().addIndent();
				} else {
					writer.add(' ');
				}
			}
		}

		// Write attributes
		int attrsCount = e.getAttributes().size();
		boolean attrNewLine = attrsCount != 1 && isPrettyPrint;
		if (attrsCount > 0) {
			writer.add(' ');
			if (isPrettyPrint) {
				writer.startLine().addIndent();
			}
		}
		Set<String> attrCache = new HashSet<>();
		for (int i = 0; i < attrsCount; i++) {
			ProtoXmlAttribute attr = e.getAttributes().get(i);

			String name = getAttributeFullName(attr, nsMap);
			if (XmlDeobf.isDuplicatedAttr(name, attrCache)) {
				continue;
			}

			writer.add(name).add("=\"").add(StringUtils.escapeXML(attr.getValue())).add('\"');

			if (i < attrsCount - 1) {
				if (attrNewLine) {
					writer.startLine().addIndent();
				} else {
					writer.add(' ');
				}
			}
		}

		if (e.getChildren().size() > 0) {
			writer.add('>');
			writer.incIndent();
			for (ProtoXmlNode child : e.getChildren()) {
				Map<String, String> oldNsMap = new HashMap<>(nsMap);
				writeNode(child, writer, oldNsMap);
			}
			writer.decIndent();
			writer.startLine("</").add(e.getName()).add('>');
		} else {
			writer.add(" />");
		}
	}

	private String getAttributeFullName(ProtoXmlAttribute a, Map<String, String> nsMap) {
		String namespaceUri = a.getNamespaceUri();
		String namespace = null;
		if (namespaceUri != null && !namespaceUri.isEmpty()) {
			namespace = nsMap.get(namespaceUri);
		}

		String attrName = a.getName();
		if (attrName == null || attrName.isEmpty()) {
			int resId = a.getResourceId();
			String str = AndroidResourcesMap.getResName(resId);
			if (str != null) {
				namespace = nsMap.get(ANDROID_NS_URL);
				int typeEnd = str.indexOf('/');
				if (typeEnd != -1) {
					attrName = str.substring(typeEnd + 1);
				} else {
					attrName = str;
				}
			} else {
				attrName = "_unknown_";
			}
		}

		return namespace != null ? namespace + ":" + attrName : attrName;
	}
}
