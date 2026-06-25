package dexforge.core.parser.dex.io;

/**
 * Utility for reading LEB128 (Little-Endian Base 128) values.
 * Standard variable-length encoding for DEX files.
 */
public final class Leb128 {
	private Leb128() {}

	/**
	 * Reads an unsigned LEB128 value from the reader.
	 */
	public static int readUleb128(DexByteReader reader) {
		int result = 0;
		int shift = 0;
		int byteValue;

		do {
			byteValue = reader.readUbyte();
			result |= (byteValue & 0x7f) << shift;
			shift += 7;
		} while ((byteValue & 0x80) != 0);

		return result;
	}

	/**
	 * Reads a signed LEB128 value from the reader.
	 */
	public static int readSleb128(DexByteReader reader) {
		int result = 0;
		int shift = 0;
		int byteValue;

		do {
			byteValue = reader.readUbyte();
			result |= (byteValue & 0x7f) << shift;
			shift += 7;
		} while ((byteValue & 0x80) != 0);

		if ((shift < 32) && ((byteValue & 0x40) != 0)) {
			result |= -(1 << shift);
		}

		return result;
	}
}
