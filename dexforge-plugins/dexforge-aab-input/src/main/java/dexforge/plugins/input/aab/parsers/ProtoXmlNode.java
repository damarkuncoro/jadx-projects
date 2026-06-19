package dexforge.plugins.input.aab.parsers;

public abstract class ProtoXmlNode {
	private final int sourceLine;

	protected ProtoXmlNode(int sourceLine) {
		this.sourceLine = sourceLine;
	}

	public int getSourceLine() {
		return sourceLine;
	}
}
