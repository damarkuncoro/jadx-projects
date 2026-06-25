package dexforge.core.parser.analysis.deobf.specific.model;

/**
 * REUSEABLE model for a decrypted string found during mass deobfuscation.
 */
public final class DecryptedString {
	private final int id;
	private final String value;
	private final String location; // ClassName->MethodName
	private final int offset;
	private final String methodSig;

	public DecryptedString(int id, String value, String location, int offset, String methodSig) {
		this.id = id;
		this.value = value;
		this.location = location;
		this.offset = offset;
		this.methodSig = methodSig;
	}

	public int getId() {
		return id;
	}

	public String getValue() {
		return value;
	}

	public String getLocation() {
		return location;
	}

	public int getOffset() {
		return offset;
	}

	public String getMethodSig() {
		return methodSig;
	}

	@Override
	public String toString() {
		return String.format("[%d] %s (found in %s at %d)", id, value, location, offset);
	}
}
