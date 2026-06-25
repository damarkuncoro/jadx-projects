package dexforge.core.parser.arsc.io;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

class ArscReaderTest {

    @Test
    void testReadNullTerminatedString() {
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.put("Hello\0World\0".getBytes());

        ArscReader reader = new ArscReader(buffer.array());

        // Read first string of size 6 (includes the null terminator)
        assertThat(reader.readString(6)).isEqualTo("Hello");
        assertThat(reader.position()).isEqualTo(6);

        // Read second string of size 6
        assertThat(reader.readString(6)).isEqualTo("World");
        assertThat(reader.position()).isEqualTo(12);
    }
}
