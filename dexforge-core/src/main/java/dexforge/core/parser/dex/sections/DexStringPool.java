package dexforge.core.parser.dex.sections;

import java.util.ArrayList;
import java.util.List;

import dexforge.core.parser.dex.io.DexByteReader;
import dexforge.core.parser.dex.io.Leb128;
import dexforge.core.parser.dex.io.Mutf8;
import dexforge.core.parser.dex.model.DexHeader;

/**
 * Efficient indexing and retrieval of strings from the DEX string_ids section.
 */
public final class DexStringPool {
	private final DexByteReader reader;
	private final int size;
	private final int offset;
	private final String[] cache;

	public DexStringPool(DexByteReader reader, DexHeader header) {
		this.reader = reader;
		this.size = header.getStringIdsSize();
		this.offset = header.getStringIdsOff();
		this.cache = new String[size];
	}

	/**
	 * Get string at specific index.
	 */
	public String getString(int index) {
		if (index < 0 || index >= size) {
			throw new IndexOutOfBoundsException("String index " + index + " out of range [0, " + size + ")");
		}
		if (cache[index] != null) {
			return cache[index];
		}

		// Read offset from string_ids array
		int stringDataOff = reader.at(offset + (index * 4)).readInt();

		// Move to string data item
		DexByteReader dataReader = reader.at(stringDataOff);
		int utf16Length = Leb128.readUleb128(dataReader);
		String value = Mutf8.decode(dataReader, utf16Length);

		cache[index] = value;
		return value;
	}

	/**
	 * Pre-load all strings into memory.
	 */
	public void preload() {
		for (int i = 0; i < size; i++) {
			getString(i);
		}
	}

	public int getSize() {
		return size;
	}
}
