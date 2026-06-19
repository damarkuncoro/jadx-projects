package dexforge.plugins.input.aab.parsers;

public class ProtoXmlTextNode extends ProtoXmlNode {
	private final String text;

	public ProtoXmlTextNode(int sourceLine, String text) {
		super(sourceLine);
		this.text = text;
	}

	public String getText() {
		return text;
	}
}
