package dexforge.core.parser.dex.sections;

import java.util.ArrayList;
import java.util.List;

import dexforge.core.parser.dex.io.DexByteReader;
import dexforge.core.parser.dex.io.Leb128;
import dexforge.core.parser.dex.model.DexEncodedField;
import dexforge.core.parser.dex.model.DexEncodedMethod;

/**
 * Parser for the class_data_item section.
 */
public final class DexClassDataParser {
	private final DexByteReader reader;

	public DexClassDataParser(DexByteReader reader) {
		this.reader = reader;
	}

	public ClassData parse(int offset) {
		if (offset == 0) {
			return null;
		}
		DexByteReader dataReader = reader.at(offset);
		int staticFieldsSize = Leb128.readUleb128(dataReader);
		int instanceFieldsSize = Leb128.readUleb128(dataReader);
		int directMethodsSize = Leb128.readUleb128(dataReader);
		int virtualMethodsSize = Leb128.readUleb128(dataReader);

		List<DexEncodedField> staticFields = readFields(dataReader, staticFieldsSize);
		List<DexEncodedField> instanceFields = readFields(dataReader, instanceFieldsSize);
		List<DexEncodedMethod> directMethods = readMethods(dataReader, directMethodsSize);
		List<DexEncodedMethod> virtualMethods = readMethods(dataReader, virtualMethodsSize);

		return new ClassData(staticFields, instanceFields, directMethods, virtualMethods);
	}

	private List<DexEncodedField> readFields(DexByteReader reader, int count) {
		List<DexEncodedField> fields = new ArrayList<>(count);
		int currentFieldIdx = 0;
		for (int i = 0; i < count; i++) {
			int diff = Leb128.readUleb128(reader);
			int accessFlags = Leb128.readUleb128(reader);
			currentFieldIdx += diff;
			fields.add(new DexEncodedField(currentFieldIdx, accessFlags));
		}
		return fields;
	}

	private List<DexEncodedMethod> readMethods(DexByteReader reader, int count) {
		List<DexEncodedMethod> methods = new ArrayList<>(count);
		int currentMethodIdx = 0;
		for (int i = 0; i < count; i++) {
			int diff = Leb128.readUleb128(reader);
			int accessFlags = Leb128.readUleb128(reader);
			int codeOff = Leb128.readUleb128(reader);
			currentMethodIdx += diff;
			methods.add(new DexEncodedMethod(currentMethodIdx, accessFlags, codeOff));
		}
		return methods;
	}

	public static final class ClassData {
		public final List<DexEncodedField> staticFields;
		public final List<DexEncodedField> instanceFields;
		public final List<DexEncodedMethod> directMethods;
		public final List<DexEncodedMethod> virtualMethods;

		public ClassData(List<DexEncodedField> staticFields, List<DexEncodedField> instanceFields,
		                 List<DexEncodedMethod> directMethods, List<DexEncodedMethod> virtualMethods) {
			this.staticFields = staticFields;
			this.instanceFields = instanceFields;
			this.directMethods = directMethods;
			this.virtualMethods = virtualMethods;
		}
	}
}
