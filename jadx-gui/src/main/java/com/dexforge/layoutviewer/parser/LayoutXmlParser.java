package com.dexforge.layoutviewer.parser;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.dexforge.layoutviewer.model.AndroidViewNode;

public class LayoutXmlParser {
	public AndroidViewNode parse(String xml) throws LayoutParseException {
		try {
			Document document = SecureXml.parse(xml);
			document.getDocumentElement().normalize();
			return parseElement(document.getDocumentElement());
		} catch (Exception e) {
			throw new LayoutParseException("Failed to parse Android XML layout", e);
		}
	}

	private AndroidViewNode parseElement(Element element) {
		AndroidViewNode viewNode = new AndroidViewNode(element.getTagName());
		NamedNodeMap attributes = element.getAttributes();
		for (int i = 0; i < attributes.getLength(); i++) {
			Node attr = attributes.item(i);
			if (!attr.getNodeName().startsWith("xmlns")) {
				viewNode.putAttribute(attr.getNodeName(), attr.getNodeValue());
			}
		}
		NodeList childNodes = element.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node child = childNodes.item(i);
			if (child instanceof Element) {
				viewNode.addChild(parseElement((Element) child));
			}
		}
		return viewNode;
	}
}
