package dexforge.core.parser.dex.io;

/**
 * Modified UTF-8 decoder for DEX files.
 */
public final class Mutf8 {
	private Mutf8() {}

	public static String decode(DexByteReader reader, int expectedUtf16Length) {
		char[] out = new char[expectedUtf16Length];
		int s = 0;
		while (s < expectedUtf16Length) {
			int a = reader.readUbyte();
			if (a < 0x80) {
				if (a == 0) {
					// End of string or error? spec says MUTF-8 doesn't use 0 byte for null
					out[s++] = (char) a;
				} else {
					out[s++] = (char) a;
				}
			} else if ((a & 0xe0) == 0xc0) {
				int b = reader.readUbyte();
				out[s++] = (char) (((a & 0x1f) << 6) | (b & 0x3f));
			} else if ((a & 0xf0) == 0xe0) {
				int b = reader.readUbyte();
				int c = reader.readUbyte();
				out[s++] = (char) (((a & 0x0f) << 12) | ((b & 0x3f) << 6) | (c & 0x3f));
			} else {
				// Unsupported or invalid sequence
				throw new IllegalArgumentException("Invalid MUTF-8 sequence");
			}
		}
		return new String(out);
	}
}
