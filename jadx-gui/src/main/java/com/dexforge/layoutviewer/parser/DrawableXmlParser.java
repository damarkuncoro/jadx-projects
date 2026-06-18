package com.dexforge.layoutviewer.parser;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.dexforge.layoutviewer.model.RenderStyle;

public class DrawableXmlParser {
	public RenderStyle parse(String xml) {
		if (xml == null || xml.isBlank()) {
			return null;
		}
		try {
			Document document = SecureXml.parse(xml);
			Element root = document.getDocumentElement();
			RenderStyle style = parseElement(root);
			return style != null && style.hasAnyValue() ? style : null;
		} catch (Exception e) {
			return null;
		}
	}

	private RenderStyle parseElement(Element element) {
		String tag = element.getTagName();
		if ("shape".equals(tag)) {
			return parseShape(element);
		}
		if ("selector".equals(tag)) {
			return parseSelector(element);
		}
		if ("layer-list".equals(tag)) {
			return parseLayerList(element);
		}
		return null;
	}

	private RenderStyle parseShape(Element shape) {
		RenderStyle style = new RenderStyle();
		NodeList children = shape.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if (child instanceof Element) {
				readShapeChild(style, (Element) child);
			}
		}
		return style;
	}

	private RenderStyle parseSelector(Element selector) {
		NodeList items = selector.getElementsByTagName("item");
		for (int i = 0; i < items.getLength(); i++) {
			Element item = (Element) items.item(i);
			NodeList children = item.getChildNodes();
			for (int j = 0; j < children.getLength(); j++) {
				Node child = children.item(j);
				if (child instanceof Element) {
					RenderStyle style = parseElement((Element) child);
					if (style != null && style.hasAnyValue()) {
						return style;
					}
				}
			}
		}
		return null;
	}

	private RenderStyle parseLayerList(Element layerList) {
		NodeList items = layerList.getElementsByTagName("item");
		for (int i = 0; i < items.getLength(); i++) {
			Element item = (Element) items.item(i);
			NodeList children = item.getChildNodes();
			for (int j = 0; j < children.getLength(); j++) {
				Node child = children.item(j);
				if (child instanceof Element) {
					RenderStyle style = parseElement((Element) child);
					if (style != null && style.hasAnyValue()) {
						return style;
					}
				}
			}
		}
		return null;
	}

	private void readShapeChild(RenderStyle style, Element child) {
		switch (child.getTagName()) {
			case "solid":
				style.setBackgroundColor(attr(child, "color"));
				break;
			case "stroke":
				style.setStrokeColor(attr(child, "color"));
				style.setStrokeWidth(attr(child, "width"));
				break;
			case "corners":
				style.setCornerRadius(firstNonEmpty(attr(child, "radius"), attr(child, "topLeftRadius")));
				break;
			case "padding":
				style.setPaddingLeft(attr(child, "left"));
				style.setPaddingTop(attr(child, "top"));
				style.setPaddingRight(attr(child, "right"));
				style.setPaddingBottom(attr(child, "bottom"));
				break;
			default:
				break;
		}
	}

	private String attr(Element element, String localName) {
		String value = element.getAttribute("android:" + localName);
		if (!value.isEmpty()) {
			return value;
		}
		value = element.getAttribute(localName);
		if (!value.isEmpty()) {
			return value;
		}
		value = element.getAttributeNS("http://schemas.android.com/apk/res/android", localName);
		return value.isEmpty() ? null : value;
	}

	private String firstNonEmpty(String first, String second) {
		if (first != null && !first.isBlank()) {
			return first;
		}
		return second;
	}
}
