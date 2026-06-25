package dexforge.core.parser.arsc.service;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

import dexforge.core.parser.arsc.io.ArscReader;
import dexforge.core.parser.arsc.model.*;
import dexforge.core.parser.arsc.parser.ArscStringPool;

/**
 * High-speed indexer for Android resources.arsc files.
 */
public final class ArscFastIndexer {
	private final ArscReader reader;
	private final Map<Integer, String> idToNameMap = new HashMap<>();
	private final List<ArscPackage> packages = new ArrayList<>();
	private ArscStringPool globalStringPool;

	public ArscFastIndexer(byte[] arscData) {
		this.reader = new ArscReader(arscData);
	}

	public void parse() {
		int type = reader.readUshort();
		if (type != 0x0002) {
			return;
		}

		int headerSize = reader.readUshort();
		int chunkSize = reader.readInt();
		int packageCount = reader.readInt();

		reader.setPosition(headerSize);

		while (reader.position() < chunkSize && reader.position() < reader.limit()) {
			int chunkPos = reader.position();
			int chunkType = reader.readUshort();
			int chunkHeaderSize = reader.readUshort();
			int chunkTotalSize = reader.readInt();

			if (chunkType == 0x0001) { // Global String Pool
				globalStringPool = new ArscStringPool(reader, chunkPos);
			} else if (chunkType == 0x0200) { // Package
				packages.add(parsePackage(chunkPos, chunkHeaderSize, chunkTotalSize));
			}

			reader.setPosition(chunkPos + chunkTotalSize);
		}
	}

	private ArscPackage parsePackage(int pos, int headerSize, int totalSize) {
		reader.setPosition(pos + 4 + 4 + 4);
		int packageId = reader.readInt();
		String packageName = reader.readUtf16String(128); // Package name is 256 bytes (UTF-16)

		ArscPackage pkg = new ArscPackage(packageId, packageName);

		reader.setPosition(pos + 12 + 256);
		int typeStringOffset = reader.readInt();
		int typeCount = reader.readInt();
		int keyStringOffset = reader.readInt();
		int keyCount = reader.readInt();

		ArscStringPool typePool = new ArscStringPool(reader, pos + typeStringOffset);
		ArscStringPool keyPool = new ArscStringPool(reader, pos + keyStringOffset);

		int currentPos = pos + totalSize;
		reader.setPosition(pos + headerSize);

		while (reader.position() < currentPos) {
			int chunkPos = reader.position();
			int chunkType = reader.readUshort();
			int chunkHeaderSize = reader.readUshort();
			int chunkTotalSize = reader.readInt();

			if (chunkType == 0x0201) { // TYPE
				try {
					ArscType typeObj = parseTypeChunk(chunkPos, packageId, typePool, keyPool);
					if (typeObj != null) {
						pkg.getTypes().add(typeObj);
					}
				} catch (Exception e) {
					// Log or handle error gracefully
				}
			}

			reader.setPosition(chunkPos + chunkTotalSize);
		}
		return pkg;
	}

	private ArscType parseTypeChunk(int pos, int packageId, ArscStringPool typePool, ArscStringPool keyPool) {
		reader.setPosition(pos + 4 + 4);
		int typeId = reader.readUbyte();
		reader.setPosition(pos + 12);
		int entryCount = reader.readInt();
		int entriesStart = reader.readInt();

		String typeName = typePool.getString(typeId - 1);
		ArscType typeObj = new ArscType(typeId, typeName);

		reader.setPosition(pos + 12 + 4 + 4 + 64);
		int[] offsets = new int[entryCount];
		for (int i = 0; i < entryCount; i++) {
			offsets[i] = reader.readInt();
		}

		for (int i = 0; i < entryCount; i++) {
			if (offsets[i] == -1) {
				continue;
			}

			int entryPos = pos + entriesStart + offsets[i];
			if (entryPos < 0 || entryPos >= reader.limit()) {
				continue;
			}

			reader.setPosition(entryPos);
			int size = reader.readUshort();
			int flags = reader.readUshort();
			int keyIdx = reader.readInt();

			int resId = (packageId << 24) | (typeId << 16) | i;
			String keyName = keyPool.getString(keyIdx);

			ArscResourceValue val = null;
			if ((flags & 0x0001) == 0) { // Check if it's not a complex entry
				val = new ArscResourceValue(reader.readUshort(), reader.readUbyte(), reader.readInt());
			}

			if (typeName != null && keyName != null) {
				idToNameMap.put(resId, "R." + typeName + "." + keyName);
				typeObj.getEntries().add(new ArscEntry(resId, keyName, val));
			}
		}
		return typeObj;
	}

	public String getResourceName(int id) {
		return idToNameMap.get(id);
	}

	public Map<Integer, String> getIdToNameMap() {
		return idToNameMap;
	}

	public List<ArscPackage> getPackages() {
		return packages;
	}
}
