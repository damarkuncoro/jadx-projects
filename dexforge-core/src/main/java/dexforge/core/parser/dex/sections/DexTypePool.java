package dexforge.core.parser.dex.sections;

import dexforge.core.parser.dex.io.DexByteReader;
import dexforge.core.parser.dex.model.DexHeader;

/**
 * High-performance access to types defined in the DEX file.
 */
public final class DexTypePool {
	private final DexByteReader reader;
	private final DexStringPool stringPool;
	private final int size;
	private final int offset;

	public DexTypePool(DexByteReader reader, DexHeader header, DexStringPool stringPool) {
		this.reader = reader;
		this.stringPool = stringPool;
		this.size = header.getTypeIdsSize();
		this.offset = header.getTypeIdsOff();
	}

	/**
	 * Get type name at specific index (e.g. "Ljava/lang/String;").
	 */
	public String getTypeName(int index) {
		if (index < 0 || index >= size) {
			throw new IndexOutOfBoundsException("Type index " + index + " out of range");
		}

		// Each entry is a uint index into the string pool
		int stringIndex = reader.at(offset + (index * 4)).readInt();
		return stringPool.getString(stringIndex);
	}

	public int getSize() {
		return size;
	}
}
