package dexforge.plugins.input.aab.parsers;

public class ProtoXmlNamespace {
	private final String prefix;
	private final String uri;

	public ProtoXmlNamespace(String prefix, String uri) {
		this.prefix = prefix;
		this.uri = uri;
	}

	public String getPrefix() {
		return prefix;
	}

	public String getUri() {
		return uri;
	}
}
