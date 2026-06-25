package dexforge.core.parser.assets;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

class SqliteParserTest {

    @Test
    void testValidSqliteFile() {
        ByteBuffer buffer = ByteBuffer.allocate(32).order(ByteOrder.LITTLE_ENDIAN);
        buffer.put("SQLite format 3\0".getBytes()); // Magic (16 bytes)
        buffer.putShort((short) 4096);               // Page size at offset 16

        SqliteParser parser = new SqliteParser(buffer.array());

        assertThat(parser.isValid()).isTrue();
        assertThat(parser.getPageSize()).isEqualTo(4096);
        assertThat(parser.getTableCount()).isEqualTo(0);
    }

    @Test
    void testInvalidSqliteFile() {
        byte[] invalidData = "Not a SQLite file".getBytes();
        SqliteParser parser = new SqliteParser(invalidData);

        assertThat(parser.isValid()).isFalse();
        assertThat(parser.getPageSize()).isEqualTo(0);
    }
}
