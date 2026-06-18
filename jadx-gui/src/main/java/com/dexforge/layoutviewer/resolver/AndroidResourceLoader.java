package com.dexforge.layoutviewer.resolver;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.dexforge.layoutviewer.parser.DrawableXmlParser;
import com.dexforge.layoutviewer.parser.SecureXml;

import jadx.api.ICodeInfo;
import jadx.api.ResourceFile;
import jadx.core.xmlgen.ResContainer;

public class AndroidResourceLoader {
	public ResourceResolver load(List<ResourceFile> resources) {
		ResourceResolver resolver = new ResourceResolver();
		for (ResourceFile resource : resources) {
			String name = normalizePath(resource.getDeobfName());
			if (name.startsWith("res/values") && name.endsWith(".xml")) {
				loadValuesXml(resolver, loadText(resource));
			} else if (isNamedResource(name, "drawable") || isNamedResource(name, "mipmap")) {
				putFileResource(resolver, name);
				if (name.endsWith(".xml")) {
					resolver.putDrawable(name, new DrawableXmlParser().parse(loadText(resource)));
				}
			}
		}
		return resolver;
	}

	private void loadValuesXml(ResourceResolver resolver, String xml) {
		if (xml == null || xml.isBlank()) {
			return;
		}
		try {
			Document document = SecureXml.parse(xml);
			NodeList childNodes = document.getDocumentElement().getChildNodes();
			for (int i = 0; i < childNodes.getLength(); i++) {
				Node child = childNodes.item(i);
				if (child instanceof Element) {
					readValueElement(resolver, (Element) child);
				}
			}
		} catch (Exception ignored) {
			// Decompiled resource XML can be malformed. Preview should stay best-effort.
		}
	}

	private void readValueElement(ResourceResolver resolver, Element element) {
		String tag = element.getTagName();
		String name = element.getAttribute("name");
		if (name.isEmpty()) {
			return;
		}
		if ("style".equals(tag)) {
			readStyleElement(resolver, name, element);
			return;
		}
		if ("color".equals(tag) || "string".equals(tag) || "dimen".equals(tag) || "drawable".equals(tag)) {
			resolver.putValue(tag, name, element.getTextContent());
		}
	}

	private void readStyleElement(ResourceResolver resolver, String name, Element styleElement) {
		Map<String, String> attrs = new LinkedHashMap<>();
		String parent = styleElement.getAttribute("parent");
		if (parent == null || parent.isBlank()) {
			int dot = name.lastIndexOf('.');
			parent = dot > 0 ? name.substring(0, dot) : null;
		}
		NodeList items = styleElement.getElementsByTagName("item");
		for (int i = 0; i < items.getLength(); i++) {
			Element item = (Element) items.item(i);
			String attrName = item.getAttribute("name");
			if (!attrName.isEmpty()) {
				attrs.put(normalizeAttrName(attrName), item.getTextContent().trim());
			}
		}
		resolver.putStyle(name, parent, attrs);
	}

	private void putFileResource(ResourceResolver resolver, String path) {
		String ref = ResourceRefs.fromPath(path);
		if (ref != null) {
			int slash = ref.indexOf('/');
			resolver.putValue(ref.substring(1, slash), ref.substring(slash + 1), path);
		}
	}

	private static String loadText(ResourceFile resource) {
		try {
			ResContainer container = resource.loadContent();
			if (container == null) {
				return "";
			}
			switch (container.getDataType()) {
				case TEXT:
				case RES_TABLE:
					ICodeInfo text = container.getText();
					return text != null ? text.getCodeStr() : "";
				default:
					return "";
			}
		} catch (Exception e) {
			return "";
		}
	}

	private static String normalizeAttrName(String name) {
		if (name.startsWith("android:")) {
			return name;
		}
		if (name.startsWith("android.")) {
			return "android:" + name.substring("android.".length());
		}
		if (!name.contains(":")) {
			return "android:" + name;
		}
		return name;
	}

	private static boolean isNamedResource(String name, String folder) {
		return name.startsWith("res/" + folder + "/")
				|| name.startsWith("res/" + folder + "-");
	}

	private static String normalizePath(String path) {
		return path.replace('\\', '/');
	}
}
