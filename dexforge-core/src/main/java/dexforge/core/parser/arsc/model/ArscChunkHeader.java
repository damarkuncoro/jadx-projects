package dexforge.core.parser.arsc.model;

public final class ArscChunkHeader {
	private final int type;
	private final int headerSize;
	private final int totalSize;
	private final int startPos;

	public ArscChunkHeader(int type, int headerSize, int totalSize, int startPos) {
		this.type = type;
		this.headerSize = headerSize;
		this.totalSize = totalSize;
		this.startPos = startPos;
	}

	public int getType() {
		return type;
	}

	public int getHeaderSize() {
		return headerSize;
	}

	public int getTotalSize() {
		return totalSize;
	}

	public int getStartPos() {
		return startPos;
	}
}
