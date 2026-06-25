package dexforge.core.parser.arsc.parser;

import static org.assertj.core.api.Assertions.assertThat;

import dexforge.core.parser.arsc.io.ArscReader;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

class ArscStringPoolTest {

    @Test
    void testParseUtf16StringPool() {
        ByteBuffer buffer = ByteBuffer.allocate(128).order(ByteOrder.LITTLE_ENDIAN);

        // Header (28 bytes)
        buffer.putShort((short) 0x0001); // Type
        buffer.putShort((short) 28);     // Header size
        buffer.putInt(60);               // Total size (28 + 8 offsets + 24 data = 60)
        buffer.putInt(2);                // String count
        buffer.putInt(0);                // Style count
        buffer.putInt(0);                // Flags (0 = UTF-16)
        buffer.putInt(36);               // Strings start (28 + 2 * 4 = 36)
        buffer.putInt(0);                // Styles start

        // Offsets array
        buffer.putInt(0);                // Offset of "Hello"
        buffer.putInt(12);               // Offset of "World" (5 chars * 2 + 2 len = 12)

        // String 0: "Hello"
        buffer.putShort((short) 5);      // UTF-16 length
        buffer.put("Hello".getBytes(StandardCharsets.UTF_16LE));

        // String 1: "World"
        buffer.putShort((short) 5);      // UTF-16 length
        buffer.put("World".getBytes(StandardCharsets.UTF_16LE));

        ArscReader reader = new ArscReader(buffer.array());
        ArscStringPool stringPool = new ArscStringPool(reader, 0);

        assertThat(stringPool.getString(0)).isEqualTo("Hello");
        assertThat(stringPool.getString(1)).isEqualTo("World");
        assertThat(stringPool.getString(2)).isNull();
        assertThat(stringPool.getString(-1)).isNull();
    }

    @Test
    void testParseUtf8StringPool() {
        ByteBuffer buffer = ByteBuffer.allocate(128).order(ByteOrder.LITTLE_ENDIAN);

        // Header (28 bytes)
        buffer.putShort((short) 0x0001); // Type
        buffer.putShort((short) 28);     // Header size
        buffer.putInt(50);               // Total size (28 + 8 offsets + 14 data = 50)
        buffer.putInt(2);                // String count
        buffer.putInt(0);                // Style count
        buffer.putInt(0x100);            // Flags (0x100 = UTF-8)
        buffer.putInt(36);               // Strings start (28 + 2 * 4 = 36)
        buffer.putInt(0);                // Styles start

        // Offsets array
        buffer.putInt(0);                // Offset of "Hello"
        buffer.putInt(7);                // Offset of "World" (1 u16Len + 1 u8Len + 5 data = 7)

        // String 0: "Hello"
        buffer.put((byte) 5);            // UTF-16 length
        buffer.put((byte) 5);            // UTF-8 length
        buffer.put("Hello".getBytes(StandardCharsets.UTF_8));

        // String 1: "World"
        buffer.put((byte) 5);            // UTF-16 length
        buffer.put((byte) 5);            // UTF-8 length
        buffer.put("World".getBytes(StandardCharsets.UTF_8));

        ArscReader reader = new ArscReader(buffer.array());
        ArscStringPool stringPool = new ArscStringPool(reader, 0);

        assertThat(stringPool.getString(0)).isEqualTo("Hello");
        assertThat(stringPool.getString(1)).isEqualTo("World");
    }
}
