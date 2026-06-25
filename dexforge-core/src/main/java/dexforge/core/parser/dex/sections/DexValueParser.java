package dexforge.core.parser.dex.sections;

import dexforge.core.parser.dex.io.DexByteReader;

/**
 * Parser for DEX encoded_value.
 */
public final class DexValueParser {

    public static Object parseValue(DexByteReader reader) {
        int argAndType = reader.readUbyte();
        int type = argAndType & 0x1F;
        int arg = (argAndType & 0xE0) >> 5;
        int size = arg + 1;

        switch (type) {
            case 0x00: // BYTE
                return (byte) reader.readByte();
            case 0x02: // SHORT
                return (short) readInt(reader, size);
            case 0x03: // CHAR
                return (char) readInt(reader, size);
            case 0x04: // INT
                return (int) readInt(reader, size);
            case 0x06: // LONG
                return readInt(reader, size);
            case 0x11: // DOUBLE
                return Double.longBitsToDouble(readInt(reader, size) << (64 - size * 8));
            case 0x17: // STRING
                return (int) readInt(reader, size); // String index
            case 0x18: // TYPE
                return (int) readInt(reader, size); // Type index
            case 0x1E: // NULL
                return null;
            case 0x1F: // BOOLEAN
                return arg == 1;
            default:
                // Skip other types for now
                reader.setPosition(reader.position() + size);
                return null;
        }
    }

    private static long readInt(DexByteReader reader, int size) {
        long res = 0;
        for (int i = 0; i < size; i++) {
            res |= ((long) reader.readUbyte()) << (i * 8);
        }
        return res;
    }
}
