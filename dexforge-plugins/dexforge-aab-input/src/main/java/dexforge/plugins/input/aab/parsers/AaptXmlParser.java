package dexforge.plugins.input.aab.parsers;

import java.io.IOException;
import java.io.InputStream;

import com.android.aapt.Resources.XmlAttribute;
import com.android.aapt.Resources.XmlElement;
import com.android.aapt.Resources.XmlNamespace;
import com.android.aapt.Resources.XmlNode;

import jadx.core.xmlgen.XmlGenUtils;

public class AaptXmlParser {
	private final AaptProtoParser protoParser;

	public AaptXmlParser(AaptProtoParser protoParser) {
		this.protoParser = protoParser;
	}

	public ProtoXmlElement parse(InputStream inputStream) throws IOException {
		XmlNode rootNode = XmlNode.parseFrom(XmlGenUtils.readData(inputStream));
		return parseNode(rootNode);
	}

	private ProtoXmlElement parseNode(XmlNode n) {
		int line = n.hasSource() ? n.getSource().getLineNumber() : 0;
		if (n.hasElement()) {
			return parseElement(n.getElement(), line);
		}
		// Fallback wrap if root node is text
		ProtoXmlElement element = new ProtoXmlElement(line, "root");
		element.getChildren().add(new ProtoXmlTextNode(line, n.getText()));
		return element;
	}

	private ProtoXmlElement parseElement(XmlElement e, int line) {
		ProtoXmlElement element = new ProtoXmlElement(line, e.getName());

		// Parse Namespaces
		for (int i = 0; i < e.getNamespaceDeclarationCount(); i++) {
			XmlNamespace ns = e.getNamespaceDeclaration(i);
			element.getNamespaces().add(new ProtoXmlNamespace(ns.getPrefix(), ns.getUri()));
		}

		// Parse Attributes
		for (int i = 0; i < e.getAttributeCount(); i++) {
			XmlAttribute attr = e.getAttribute(i);
			String val = attr.getValue();
			if (val.isEmpty()) {
				val = protoParser.parse(attr.getCompiledItem());
			}
			element.getAttributes().add(new ProtoXmlAttribute(attr.getName(), val, attr.getNamespaceUri(), attr.getResourceId()));
		}

		// Parse Children
		for (int i = 0; i < e.getChildCount(); i++) {
			XmlNode childNode = e.getChild(i);
			int childLine = childNode.hasSource() ? childNode.getSource().getLineNumber() : line;
			if (childNode.hasElement()) {
				element.getChildren().add(parseElement(childNode.getElement(), childLine));
			} else {
				element.getChildren().add(new ProtoXmlTextNode(childLine, childNode.getText()));
			}
		}

		return element;
	}
}
