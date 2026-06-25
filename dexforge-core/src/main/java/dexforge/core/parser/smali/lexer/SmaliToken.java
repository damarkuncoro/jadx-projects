package dexforge.core.parser.smali.lexer;

public final class SmaliToken {
	private final SmaliTokenType type;
	private final String text;
	private final int line;
	private final int column;

	public SmaliToken(SmaliTokenType type, String text, int line, int column) {
		this.type = type;
		this.text = text;
		this.line = line;
		this.column = column;
	}

	public SmaliTokenType getType() {
		return type;
	}

	public String getText() {
		return text;
	}

	public int getLine() {
		return line;
	}

	public int getColumn() {
		return column;
	}

	@Override
	public String toString() {
		return String.format("Token[%s, '%s', %d:%d]", type, text, line, column);
	}
}
