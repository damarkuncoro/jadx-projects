package dexforge.engine;

public final class DexForgeSourcePosition {
	private final int line;
	private final int character;

	public DexForgeSourcePosition(int line, int character) {
		this.line = line;
		this.character = character;
	}

	public int getLine() {
		return line;
	}

	public int getCharacter() {
		return character;
	}
}
