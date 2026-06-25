package dexforge.core.parser.arsc.parser;

import dexforge.core.parser.arsc.io.ArscReader;
import java.nio.charset.StandardCharsets;

public final class ArscStringPool {
    private final String[] strings;

    public ArscStringPool(ArscReader reader, int offset) {
        reader.setPosition(offset);
        int type = reader.readUshort();
        int headerSize = reader.readUshort();
        int totalSize = reader.readInt();
        int stringCount = reader.readInt();
        int styleCount = reader.readInt();
        int flags = reader.readInt();
        int stringsStart = reader.readInt();
        int stylesStart = reader.readInt();

        boolean isUtf8 = (flags & 0x100) != 0;
        int[] offsets = new int[stringCount];
        for (int i = 0; i < stringCount; i++) {
            offsets[i] = reader.readInt();
        }

        strings = new String[stringCount];
        int baseAddr = offset + stringsStart;
        for (int i = 0; i < stringCount; i++) {
            reader.setPosition(baseAddr + offsets[i]);
            if (isUtf8) {
                int u16Len = readUtf8Len(reader);
                int u8Len = readUtf8Len(reader);
                byte[] data = reader.readByteArray(u8Len);
                strings[i] = new String(data, StandardCharsets.UTF_8);
            } else {
                int u16Len = readUtf16Len(reader);
                byte[] data = reader.readByteArray(u16Len * 2);
                strings[i] = new String(data, StandardCharsets.UTF_16LE);
            }
        }
    }

    private int readUtf8Len(ArscReader reader) {
        int len = reader.readUbyte();
        if ((len & 0x80) != 0) {
            len = ((len & 0x7F) << 8) | reader.readUbyte();
        }
        return len;
    }

    private int readUtf16Len(ArscReader reader) {
        int len = reader.readUshort();
        if ((len & 0x8000) != 0) {
            len = ((len & 0x7FFF) << 16) | reader.readUshort();
        }
        return len;
    }

    public String getString(int index) {
        if (index < 0 || index >= strings.length) return null;
        return strings[index];
    }
}
