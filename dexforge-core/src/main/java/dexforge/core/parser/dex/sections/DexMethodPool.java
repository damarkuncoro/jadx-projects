package dexforge.core.parser.dex.sections;

import dexforge.core.parser.dex.io.DexByteReader;
import dexforge.core.parser.dex.model.DexHeader;

/**
 * Access to method definitions in the DEX file.
 */
public final class DexMethodPool {
	private final DexByteReader reader;
	private final DexTypePool typePool;
	private final DexStringPool stringPool;
	private final DexProtoPool protoPool;
	private final int size;
	private final int offset;

	public DexMethodPool(DexByteReader reader, DexHeader header, DexTypePool typePool, DexStringPool stringPool, DexProtoPool protoPool) {
		this.reader = reader;
		this.typePool = typePool;
		this.stringPool = stringPool;
		this.protoPool = protoPool;
		this.size = header.getMethodIdsSize();
		this.offset = header.getMethodIdsOff();
	}

	/**
	 * Get method name at specific index.
	 */
	public String getMethodName(int index) {
		checkIndex(index);
		// method_id_item: class_idx (2), proto_idx (2), name_idx (4)
		int nameIdx = reader.at(offset + (index * 8) + 4).readInt();
		return stringPool.getString(nameIdx);
	}

	/**
	 * Get declaring class of the method at specific index.
	 */
	public String getDeclaringClass(int index) {
		checkIndex(index);
		int classIdx = reader.at(offset + (index * 8)).readUshort();
		return typePool.getTypeName(classIdx);
	}

	/**
	 * Get method prototype at specific index.
	 */
	public int getProtoIndex(int index) {
		checkIndex(index);
		return reader.at(offset + (index * 8) + 2).readUshort();
	}

	public DexProtoPool getProtoPool() {
		return protoPool;
	}

	/**
	 * Get full method signature (e.g. "Ljava/lang/Object;->toString()Ljava/lang/String;").
	 */
	public String getMethodSignature(int index) {
		String declaringClass = getDeclaringClass(index);
		String name = getMethodName(index);
		int protoIdx = getProtoIndex(index);
		String returnType = protoPool.getReturnType(protoIdx);
		java.util.List<String> params = protoPool.getParameters(protoIdx);

		StringBuilder sb = new StringBuilder();
		sb.append(declaringClass).append("->").append(name).append("(");
		for (String param : params) {
			sb.append(param);
		}
		sb.append(")").append(returnType);
		return sb.toString();
	}

	public int getSize() {
		return size;
	}

	private void checkIndex(int index) {
		if (index < 0 || index >= size) {
			throw new IndexOutOfBoundsException("Method index " + index + " out of range");
		}
	}
}
