package dexforge.core.parser.dex.sections;

import dexforge.core.parser.dex.io.DexByteReader;
import dexforge.core.parser.dex.model.DexHeader;

/**
 * Access to field definitions in the DEX file.
 */
public final class DexFieldPool {
	private final DexByteReader reader;
	private final DexTypePool typePool;
	private final DexStringPool stringPool;
	private final int size;
	private final int offset;

	public DexFieldPool(DexByteReader reader, DexHeader header, DexTypePool typePool, DexStringPool stringPool) {
		this.reader = reader;
		this.typePool = typePool;
		this.stringPool = stringPool;
		this.size = header.getFieldIdsSize();
		this.offset = header.getFieldIdsOff();
	}

	/**
	 * Get field name at specific index.
	 */
	public String getFieldName(int index) {
		checkIndex(index);
		// field_id_item: class_idx (2), type_idx (2), name_idx (4)
		int nameIdx = reader.at(offset + (index * 8) + 4).readInt();
		return stringPool.getString(nameIdx);
	}

	/**
	 * Get field type at specific index.
	 */
	public String getFieldType(int index) {
		checkIndex(index);
		int typeIdx = reader.at(offset + (index * 8) + 2).readUshort();
		return typePool.getTypeName(typeIdx);
	}

	/**
	 * Get declaring class of the field at specific index.
	 */
	public String getDeclaringClass(int index) {
		checkIndex(index);
		int classIdx = reader.at(offset + (index * 8)).readUshort();
		return typePool.getTypeName(classIdx);
	}

	public String getFieldClassName(int index) {
		return getDeclaringClass(index);
	}

	public String getFieldSignature(int index) {
		checkIndex(index);
		return getDeclaringClass(index) + "->" + getFieldName(index) + ":" + getFieldType(index);
	}

	public int getSize() {
		return size;
	}

	private void checkIndex(int index) {
		if (index < 0 || index >= size) {
			throw new IndexOutOfBoundsException("Field index " + index + " out of range");
		}
	}
}
