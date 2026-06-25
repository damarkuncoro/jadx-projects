package dexforge.core.parser.dex.model;

/**
 * Represents a method defined within a class.
 */
public final class DexEncodedMethod {
	private final int methodIndex;
	private final int accessFlags;
	private final int codeOff;

	public DexEncodedMethod(int methodIndex, int accessFlags, int codeOff) {
		this.methodIndex = methodIndex;
		this.accessFlags = accessFlags;
		this.codeOff = codeOff;
	}

	public int getMethodIndex() { return methodIndex; }
	public int getAccessFlags() { return accessFlags; }
	public int getCodeOff() { return codeOff; }
}
