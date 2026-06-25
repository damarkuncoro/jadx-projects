package dexforge.core.parser.dex.model;

/**
 * Represents a field defined within a class.
 */
public final class DexEncodedField {
	private final int fieldIndex;
	private final int accessFlags;

	public DexEncodedField(int fieldIndex, int accessFlags) {
		this.fieldIndex = fieldIndex;
		this.accessFlags = accessFlags;
	}

	public int getFieldIndex() { return fieldIndex; }
	public int getAccessFlags() { return accessFlags; }
}
