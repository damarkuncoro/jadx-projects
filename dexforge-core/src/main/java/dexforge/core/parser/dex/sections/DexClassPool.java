package dexforge.core.parser.dex.sections;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dexforge.core.parser.dex.io.DexByteReader;
import dexforge.core.parser.dex.model.DexClass;
import dexforge.core.parser.dex.model.DexHeader;

/**
 * Access to class definitions in the DEX file.
 */
public final class DexClassPool {
	private final DexByteReader reader;
	private final DexHeader header;
	private final DexTypePool typePool;
	private final DexStringPool stringPool;
	private final int size;
	private final int offset;

	public DexClassPool(DexByteReader reader, DexHeader header, DexTypePool typePool, DexStringPool stringPool) {
		this.reader = reader;
		this.header = header;
		this.typePool = typePool;
		this.stringPool = stringPool;
		this.size = header.getClassDefsSize();
		this.offset = header.getClassDefsOff();
	}

	/**
	 * Get class definition at specific index.
	 */
	public DexClass getClassDef(int index) {
		if (index < 0 || index >= size) {
			throw new IndexOutOfBoundsException("Class index " + index + " out of range");
		}

		DexByteReader itemReader = reader.at(offset + (index * 32));

		int classIdx = itemReader.readInt();
		int accessFlags = itemReader.readInt();
		int superclassIdx = itemReader.readInt();
		int interfacesOff = itemReader.readInt();
		int sourceFileIdx = itemReader.readInt();
		int annotationsOff = itemReader.readInt();
		int classDataOff = itemReader.readInt();
		int staticValuesOff = itemReader.readInt();

		String name = typePool.getTypeName(classIdx);
		String superclass = (superclassIdx == -1) ? null : typePool.getTypeName(superclassIdx);
		String sourceFile = (sourceFileIdx == -1) ? null : stringPool.getString(sourceFileIdx);
		List<String> interfaces = parseInterfaces(interfacesOff);

		return new DexClass(name, accessFlags, superclass, interfaces, sourceFile, classDataOff, annotationsOff, staticValuesOff);
	}

	private List<String> parseInterfaces(int offset) {
		if (offset == 0) {
			return Collections.emptyList();
		}
		DexByteReader listReader = reader.at(offset);
		int count = listReader.readInt();
		List<String> interfaces = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			interfaces.add(typePool.getTypeName(listReader.readUshort()));
		}
		return interfaces;
	}

	public int getSize() {
		return size;
	}
}
