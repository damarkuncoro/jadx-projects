package dexforge.plugins.input.aab.parsers;

public class ProtoXmlAttribute {
	private String name;
	private String value;
	private final String namespaceUri;
	private final int resourceId;

	public ProtoXmlAttribute(String name, String value, String namespaceUri, int resourceId) {
		this.name = name;
		this.value = value;
		this.namespaceUri = namespaceUri;
		this.resourceId = resourceId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getNamespaceUri() {
		return namespaceUri;
	}

	public int getResourceId() {
		return resourceId;
	}
}
