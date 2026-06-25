package dexforge.core.parser.common.io;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

class BinaryReaderTest {

    @Test
    void testReadOperations() {
        ByteBuffer buffer = ByteBuffer.allocate(32).order(ByteOrder.LITTLE_ENDIAN);
        buffer.put((byte) 0x12);            // byte
        buffer.put((byte) 0xFE);            // ubyte -> 254
        buffer.putShort((short) 0x1234);     // short
        buffer.putShort((short) 0xFEDC);     // ushort -> 65244
        buffer.putInt(0x12345678);           // int
        buffer.putInt(0xFFFFFFFF);           // uint -> 4294967295L
        buffer.putLong(0x1122334455667788L); // long

        // UTF-16LE String: "Hi" (4 bytes)
        buffer.put("H".getBytes(java.nio.charset.StandardCharsets.UTF_16LE));
        buffer.put("i".getBytes(java.nio.charset.StandardCharsets.UTF_16LE));

        BinaryReader reader = new BinaryReader(buffer.array());

        assertThat(reader.position()).isEqualTo(0);
        assertThat(reader.limit()).isEqualTo(32);

        assertThat(reader.readByte()).isEqualTo((byte) 0x12);
        assertThat(reader.readUbyte()).isEqualTo(254);
        assertThat(reader.readShort()).isEqualTo((short) 0x1234);
        assertThat(reader.readUshort()).isEqualTo(65244);
        assertThat(reader.readInt()).isEqualTo(0x12345678);
        assertThat(reader.readUint()).isEqualTo(4294967295L);
        assertThat(reader.readLong()).isEqualTo(0x1122334455667788L);

        assertThat(reader.readUtf16String(2)).isEqualTo("Hi");

        reader.setPosition(0);
        assertThat(reader.position()).isEqualTo(0);
        assertThat(reader.readByteArray(2)).containsExactly((byte) 0x12, (byte) 0xFE);
    }
}
