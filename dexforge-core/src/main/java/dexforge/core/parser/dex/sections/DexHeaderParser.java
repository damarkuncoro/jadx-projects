package dexforge.core.parser.dex.sections;

import java.nio.charset.StandardCharsets;
import java.util.zip.Adler32;

import dexforge.core.parser.dex.io.DexByteReader;
import dexforge.core.parser.dex.model.DexHeader;

/**
 * Parser for the DEX file header.
 * Includes integrity verification logic.
 */
public final class DexHeaderParser {
	private final DexByteReader reader;

	public DexHeaderParser(DexByteReader reader) {
		this.reader = reader;
	}

	public DexHeader parse() {
		DexHeader header = new DexHeader();
		reader.setPosition(0);

		// 1. Magic (8 bytes)
		header.setMagic(new String(reader.readByteArray(8), StandardCharsets.US_ASCII));

		// 2. Checksum (4 bytes)
		header.setChecksum(reader.readUint());

		// 3. Signature (20 bytes)
		header.setSignature(reader.readByteArray(20));

		// 4. Sizes and Offsets
		header.setFileSize(reader.readUint());
		header.setHeaderSize(reader.readUint());

		// Skip Endian Tag (4 bytes)
		reader.readUint();

		// Map link size/off (8 bytes)
		reader.readUint();
		reader.readUint();

		// Map off
		reader.readUint();

		header.setStringIdsSize(reader.readInt());
		header.setStringIdsOff(reader.readInt());

		header.setTypeIdsSize(reader.readInt());
		header.setTypeIdsOff(reader.readInt());

		header.setProtoIdsSize(reader.readInt());
		header.setProtoIdsOff(reader.readInt());

		header.setFieldIdsSize(reader.readInt());
		header.setFieldIdsOff(reader.readInt());

		header.setMethodIdsSize(reader.readInt());
		header.setMethodIdsOff(reader.readInt());

		header.setClassDefsSize(reader.readInt());
		header.setClassDefsOff(reader.readInt());

		header.setDataSize(reader.readInt());
		header.setDataOff(reader.readInt());

		return header;
	}

	/**
	 * Verify Adler-32 checksum of the DEX file.
	 * Checksum is calculated over all bytes except magic and checksum itself.
	 */
	public boolean verifyChecksum(DexHeader header) {
		Adler32 adler = new Adler32();
		byte[] allData = reader.at(12).readByteArray((int) header.getFileSize() - 12);
		adler.update(allData);
		return adler.getValue() == header.getChecksum();
	}
}
