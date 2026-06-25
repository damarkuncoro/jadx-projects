package dexforge.core.parser.dex.sections;

import static org.assertj.core.api.Assertions.assertThat;

import dexforge.core.parser.dex.io.DexByteReader;
import dexforge.core.parser.dex.model.DexHeader;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

class DexHeaderParserTest {

    @Test
    void testParseValidHeader() {
        // Create a minimal valid DEX header mock (112 bytes)
        ByteBuffer buffer = ByteBuffer.allocate(112).order(ByteOrder.LITTLE_ENDIAN);
        buffer.put("dex\n035\0".getBytes()); // Magic
        buffer.putInt(0x12345678);           // Checksum
        buffer.put(new byte[20]);             // Signature
        buffer.putInt(112);                  // File Size
        buffer.putInt(112);                  // Header Size
        buffer.putInt(0x12345678);           // Endian Tag
        buffer.putInt(0);                    // Link Size
        buffer.putInt(0);                    // Link Off
        buffer.putInt(0);                    // Map Off
        buffer.putInt(5);                    // String IDs Size
        buffer.putInt(112);                  // String IDs Off

        DexByteReader reader = new DexByteReader(buffer.array());
        DexHeaderParser parser = new DexHeaderParser(reader);

        DexHeader header = parser.parse();

        assertThat(header.getMagic()).isEqualTo("dex\n035\0");
        assertThat(header.getFileSize()).isEqualTo(112);
        assertThat(header.getStringIdsSize()).isEqualTo(5);
    }
}
