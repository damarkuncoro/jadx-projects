package dexforge.core.parser.dex.model;

/**
 * Value Object representing the DEX file header (112 bytes).
 * Ref: https://source.android.com/docs/core/runtime/dex-format#header-item
 */
public final class DexHeader {
	private String magic;
	private long checksum;
	private byte[] signature;
	private long fileSize;
	private long headerSize;
	private long endianTag;

	private int stringIdsSize;
	private int stringIdsOff;

	private int typeIdsSize;
	private int typeIdsOff;

	private int protoIdsSize;
	private int protoIdsOff;

	private int fieldIdsSize;
	private int fieldIdsOff;

	private int methodIdsSize;
	private int methodIdsOff;

	private int classDefsSize;
	private int classDefsOff;

	private int dataSize;
	private int dataOff;

	// Getters and Setters
	public String getMagic() { return magic; }
	public void setMagic(String magic) { this.magic = magic; }

	public long getChecksum() { return checksum; }
	public void setChecksum(long checksum) { this.checksum = checksum; }

	public byte[] getSignature() { return signature; }
	public void setSignature(byte[] signature) { this.signature = signature; }

	public long getFileSize() { return fileSize; }
	public void setFileSize(long fileSize) { this.fileSize = fileSize; }

	public long getHeaderSize() { return headerSize; }
	public void setHeaderSize(long headerSize) { this.headerSize = headerSize; }

	public int getStringIdsSize() { return stringIdsSize; }
	public void setStringIdsSize(int stringIdsSize) { this.stringIdsSize = stringIdsSize; }

	public int getStringIdsOff() { return stringIdsOff; }
	public void setStringIdsOff(int stringIdsOff) { this.stringIdsOff = stringIdsOff; }

	public int getTypeIdsSize() { return typeIdsSize; }
	public void setTypeIdsSize(int typeIdsSize) { this.typeIdsSize = typeIdsSize; }

	public int getTypeIdsOff() { return typeIdsOff; }
	public void setTypeIdsOff(int typeIdsOff) { this.typeIdsOff = typeIdsOff; }

	public int getClassDefsSize() { return classDefsSize; }
	public void setClassDefsSize(int classDefsSize) { this.classDefsSize = classDefsSize; }

	public int getClassDefsOff() { return classDefsOff; }
	public void setClassDefsOff(int classDefsOff) { this.classDefsOff = classDefsOff; }

	public int getMethodIdsSize() { return methodIdsSize; }
	public void setMethodIdsSize(int methodIdsSize) { this.methodIdsSize = methodIdsSize; }

	public int getMethodIdsOff() { return methodIdsOff; }
	public void setMethodIdsOff(int methodIdsOff) { this.methodIdsOff = methodIdsOff; }

	public int getFieldIdsSize() { return fieldIdsSize; }
	public void setFieldIdsSize(int fieldIdsSize) { this.fieldIdsSize = fieldIdsSize; }

	public int getFieldIdsOff() { return fieldIdsOff; }
	public void setFieldIdsOff(int fieldIdsOff) { this.fieldIdsOff = fieldIdsOff; }

	public int getProtoIdsSize() { return protoIdsSize; }
	public void setProtoIdsSize(int protoIdsSize) { this.protoIdsSize = protoIdsSize; }

	public int getProtoIdsOff() { return protoIdsOff; }
	public void setProtoIdsOff(int protoIdsOff) { this.protoIdsOff = protoIdsOff; }

	public int getDataSize() { return dataSize; }
	public void setDataSize(int dataSize) { this.dataSize = dataSize; }

	public int getDataOff() { return dataOff; }
	public void setDataOff(int dataOff) { this.dataOff = dataOff; }
}
