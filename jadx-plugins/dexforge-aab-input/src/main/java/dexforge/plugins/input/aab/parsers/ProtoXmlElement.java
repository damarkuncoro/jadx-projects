package dexforge.plugins.input.aab.parsers;

import java.util.ArrayList;
import java.util.List;

public class ProtoXmlElement extends ProtoXmlNode {
	private String name;
	private final List<ProtoXmlNamespace> namespaces = new ArrayList<>();
	private final List<ProtoXmlAttribute> attributes = new ArrayList<>();
	private final List<ProtoXmlNode> children = new ArrayList<>();

	public ProtoXmlElement(int sourceLine, String name) {
		super(sourceLine);
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<ProtoXmlNamespace> getNamespaces() {
		return namespaces;
	}

	public List<ProtoXmlAttribute> getAttributes() {
		return attributes;
	}

	public List<ProtoXmlNode> getChildren() {
		return children;
	}
}
