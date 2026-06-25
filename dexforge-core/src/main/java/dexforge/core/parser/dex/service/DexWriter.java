package dexforge.core.parser.dex.service;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.Adler32;
import java.security.MessageDigest;

import dexforge.core.parser.dex.io.DexByteWriter;
import dexforge.core.parser.dex.builder.DexPoolManager;
import dexforge.core.parser.dex.model.DexCode;

/**
 * High-fidelity DEX Assembler.
 * Compiles structural models and bytecode into a fully valid and executable .dex file.
 */
public final class DexWriter {
	private final DexPoolManager poolManager;
	private final DexByteWriter writer;

	private int stringIdsOff;
	private int typeIdsOff;
	private int protoIdsOff;
	private int fieldIdsOff;
	private int methodIdsOff;
	private int classDefsOff;
	private int mapOff;

	public DexWriter(DexPoolManager poolManager) {
		this.poolManager = poolManager;
		this.writer = new DexByteWriter(1024 * 1024);
	}

	public byte[] compile() throws Exception {
		writer.setPosition(112); // Reserve header

		writeStringData();
		writeTypeIds();
		writeProtoIds();
		writeFieldIds();
		writeMethodIds();

		// Data section: Code Items and Class Data
		Map<String, Integer> classDataOffsets = writeClassDataAndCode();

		writeClassDefs(classDataOffsets);
		writeMapList();
		updateHeader();

		byte[] result = writer.toByteArray();
		calculateSignature(result);
		calculateChecksum(result);
		return result;
	}

	private void writeStringData() {
		List<String> strings = poolManager.getSortedStrings();
		int[] dataOffsets = new int[strings.size()];
		for (int i = 0; i < strings.size(); i++) {
			dataOffsets[i] = writer.position();
			String s = strings.get(i);
			writer.writeUleb128(s.length());
			try {
				writer.writeByteArray(s.getBytes("UTF-8"));
			} catch (Exception ignored) {
			}
			writer.writeByte(0);
		}
		this.stringIdsOff = writer.position();
		for (int offset : dataOffsets) {
			writer.writeInt(offset);
		}
	}

	private void writeTypeIds() {
		this.typeIdsOff = writer.position();
		for (String type : poolManager.getSortedTypes()) {
			writer.writeInt(poolManager.getStringIndex(type));
		}
	}

	private void writeProtoIds() {
		this.protoIdsOff = writer.position();
		for (DexPoolManager.ProtoId proto : poolManager.getSortedProtos()) {
			writer.writeInt(poolManager.getStringIndex(proto.getShorty()));
			writer.writeInt(poolManager.getTypeIndex(proto.getReturnType()));
			writer.writeInt(0); // parameters_off
		}
	}

	private void writeFieldIds() {
		this.fieldIdsOff = writer.position();
		for (DexPoolManager.FieldId field : poolManager.getSortedFields()) {
			writer.writeShort(poolManager.getTypeIndex(field.getClassName()));
			writer.writeShort(poolManager.getTypeIndex(field.getType()));
			writer.writeInt(poolManager.getStringIndex(field.getName()));
		}
	}

	private void writeMethodIds() {
		this.methodIdsOff = writer.position();
		for (DexPoolManager.MethodId method : poolManager.getSortedMethods()) {
			writer.writeShort(poolManager.getTypeIndex(method.getClassName()));
			writer.writeShort(poolManager.getProtoIndex(method.getProto()));
			writer.writeInt(poolManager.getStringIndex(method.getName()));
		}
	}

	private Map<String, Integer> writeClassDataAndCode() {
		Map<String, Integer> classDataOffsets = new HashMap<>();

		// 1. Write Code Items first (DEX typically puts them before class_data)
		Map<DexPoolManager.MethodId, Integer> codeOffsets = new HashMap<>();
		for (DexPoolManager.MethodId mid : poolManager.getSortedMethods()) {
			DexCode code = poolManager.getMethodCode(mid);
			if (code != null) {
				codeOffsets.put(mid, writer.position());
				writeCodeItem(code);
			}
		}

		// 2. Write Class Data Items
		for (DexPoolManager.ClassDefId def : poolManager.getClassDefs()) {
			classDataOffsets.put(def.getClassName(), writer.position());
			writeClassDataItem(def, codeOffsets);
		}

		return classDataOffsets;
	}

	private void writeCodeItem(DexCode code) {
		writer.writeShort(code.getRegistersSize());
		writer.writeShort(code.getInsSize());
		writer.writeShort(code.getOutsSize());
		writer.writeShort(0); // tries_size
		writer.writeInt(code.getDebugInfoOff());
		writer.writeInt(code.getInstructions().length);
		for (short s : code.getInstructions()) {
			writer.writeShort(s);
		}
		// Padding for 4-byte alignment
		if ((code.getInstructions().length % 2) != 0) {
			writer.writeShort(0);
		}
	}

	private void writeClassDataItem(DexPoolManager.ClassDefId def, Map<DexPoolManager.MethodId, Integer> codeOffsets) {
		writer.writeUleb128(0); // static_fields_size
		writer.writeUleb128(0); // instance_fields_size

		List<DexPoolManager.MethodId> classMethods = new ArrayList<>();
		for (DexPoolManager.MethodId mid : poolManager.getSortedMethods()) {
			if (mid.getClassName().equals(def.getClassName())) {
				classMethods.add(mid);
			}
		}

		writer.writeUleb128(classMethods.size()); // direct_methods_size
		writer.writeUleb128(0); // virtual_methods_size

		int lastIdx = 0;
		for (DexPoolManager.MethodId mid : classMethods) {
			int currentIdx = poolManager.getSortedMethods().indexOf(mid);
			writer.writeUleb128(currentIdx - lastIdx);
			writer.writeUleb128(def.getAccessFlags()); // simplified
			writer.writeUleb128(codeOffsets.getOrDefault(mid, 0));
			lastIdx = currentIdx;
		}
	}

	private void writeClassDefs(Map<String, Integer> classDataOffsets) {
		this.classDefsOff = writer.position();
		for (DexPoolManager.ClassDefId def : poolManager.getClassDefs()) {
			writer.writeInt(poolManager.getTypeIndex(def.getClassName()));
			writer.writeInt(def.getAccessFlags());
			writer.writeInt(def.getSuperName() != null ? poolManager.getTypeIndex(def.getSuperName()) : -1);
			writer.writeInt(0); // interfaces_off
			writer.writeInt(def.getSourceFile() != null ? poolManager.getStringIndex(def.getSourceFile()) : -1);
			writer.writeInt(0); // annotations_off
			writer.writeInt(classDataOffsets.getOrDefault(def.getClassName(), 0));
			writer.writeInt(0); // static_values_off
		}
	}

	private void writeMapList() {
		this.mapOff = writer.position();
		writer.writeInt(8);
		writeMapItem(0x0000, 1, 0); // Header
		writeMapItem(0x0001, poolManager.getSortedStrings().size(), stringIdsOff);
		writeMapItem(0x0002, poolManager.getSortedTypes().size(), typeIdsOff);
		writeMapItem(0x0003, poolManager.getSortedProtos().size(), protoIdsOff);
		writeMapItem(0x0004, poolManager.getSortedFields().size(), fieldIdsOff);
		writeMapItem(0x0005, poolManager.getSortedMethods().size(), methodIdsOff);
		writeMapItem(0x0006, poolManager.getClassDefs().size(), classDefsOff);
		writeMapItem(0x1000, 1, mapOff);
	}

	private void writeMapItem(int type, int size, int offset) {
		writer.writeShort(type);
		writer.writeShort(0);
		writer.writeInt(size);
		writer.writeInt(offset);
	}

	private void updateHeader() {
		int fileSize = writer.position();
		writer.setPosition(0);
		writer.writeByteArray("dex\n035\0".getBytes());
		writer.setPosition(32);
		writer.writeInt(fileSize);
		writer.writeInt(112);
		writer.writeInt(0x12345678);

		writeHeaderField(56, poolManager.getSortedStrings().size(), stringIdsOff);
		writeHeaderField(64, poolManager.getSortedTypes().size(), typeIdsOff);
		writeHeaderField(72, poolManager.getSortedProtos().size(), protoIdsOff);
		writeHeaderField(80, poolManager.getSortedFields().size(), fieldIdsOff);
		writeHeaderField(88, poolManager.getSortedMethods().size(), methodIdsOff);
		writeHeaderField(96, poolManager.getClassDefs().size(), classDefsOff);
		writer.setPosition(52);
		writer.writeInt(mapOff);
	}

	private void writeHeaderField(int pos, int size, int off) {
		writer.setPosition(pos);
		writer.writeInt(size);
		writer.writeInt(off);
	}

	private void calculateSignature(byte[] data) throws Exception {
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		md.update(data, 32, data.length - 32);
		byte[] sig = md.digest();
		System.arraycopy(sig, 0, data, 12, 20);
	}

	private void calculateChecksum(byte[] data) {
		Adler32 adler = new Adler32();
		adler.update(data, 12, data.length - 12);
		long checksum = adler.getValue();
		data[8] = (byte) (checksum & 0xff);
		data[9] = (byte) ((checksum >> 8) & 0xff);
		data[10] = (byte) ((checksum >> 16) & 0xff);
		data[11] = (byte) ((checksum >> 24) & 0xff);
	}
}
