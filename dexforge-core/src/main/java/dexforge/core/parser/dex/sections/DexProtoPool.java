package dexforge.core.parser.dex.sections;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dexforge.core.parser.dex.io.DexByteReader;
import dexforge.core.parser.dex.model.DexHeader;

/**
 * Access to method prototype definitions.
 */
public final class DexProtoPool {
	private final DexByteReader reader;
	private final DexTypePool typePool;
	private final DexStringPool stringPool;
	private final int size;
	private final int offset;

	public DexProtoPool(DexByteReader reader, DexHeader header, DexTypePool typePool, DexStringPool stringPool) {
		this.reader = reader;
		this.typePool = typePool;
		this.stringPool = stringPool;
		this.size = header.getProtoIdsSize();
		this.offset = header.getProtoIdsOff();
	}

	/**
	 * Get "shorty" descriptor for the prototype (e.g. "VIII").
	 */
	public String getShorty(int index) {
		checkIndex(index);
		int shortyIdx = reader.at(offset + (index * 12)).readInt();
		return stringPool.getString(shortyIdx);
	}

	/**
	 * Get return type descriptor.
	 */
	public String getReturnType(int index) {
		checkIndex(index);
		int typeIdx = reader.at(offset + (index * 12) + 4).readInt();
		return typePool.getTypeName(typeIdx);
	}

	/**
	 * Get parameter type descriptors.
	 */
	public List<String> getParameters(int index) {
		checkIndex(index);
		int paramsOff = reader.at(offset + (index * 12) + 8).readInt();
		if (paramsOff == 0) {
			return Collections.emptyList();
		}

		DexByteReader listReader = reader.at(paramsOff);
		int count = listReader.readInt();
		List<String> params = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			int typeIdx = listReader.readUshort();
			params.add(typePool.getTypeName(typeIdx));
		}
		return params;
	}

	public int getSize() {
		return size;
	}

	private void checkIndex(int index) {
		if (index < 0 || index >= size) {
			throw new IndexOutOfBoundsException("Proto index " + index + " out of range");
		}
	}
}
