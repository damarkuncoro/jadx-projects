package dexforge.core.parser.assets;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

class ProtobufParserTest {

    @Test
    void testParseProtobuf() {
        ByteBuffer buffer = ByteBuffer.allocate(64).order(ByteOrder.LITTLE_ENDIAN);
        
        // Field 1: Varint (value 150) -> Tag = 1 << 3 | 0 = 8. ULEB128 for 150 = [0x96, 0x01]
        buffer.put((byte) 8);
        buffer.put((byte) 0x96);
        buffer.put((byte) 0x01);

        // Field 2: 32-bit (value 0x12345678) -> Tag = 2 << 3 | 5 = 21. 
        buffer.put((byte) 21);
        buffer.putInt(0x12345678);

        // Field 3: 64-bit (value 0x1122334455667788L) -> Tag = 3 << 3 | 1 = 25.
        buffer.put((byte) 25);
        buffer.putLong(0x1122334455667788L);

        // Field 4: Length-delimited (value "hello") -> Tag = 4 << 3 | 2 = 34. Length = 5.
        buffer.put((byte) 34);
        buffer.put((byte) 5);
        buffer.put("hello".getBytes());

        byte[] data = new byte[buffer.position()];
        buffer.flip();
        buffer.get(data);

        ProtobufParser parser = new ProtobufParser(data);
        List<ProtobufParser.ProtobufField> fields = parser.parse();

        assertThat(fields).hasSize(4);

        // Assert Field 1
        ProtobufParser.ProtobufField f1 = fields.get(0);
        assertThat(f1.getNumber()).isEqualTo(1);
        assertThat(f1.getWireType()).isEqualTo(0);
        assertThat(f1.getValue()).isEqualTo(150);

        // Assert Field 2
        ProtobufParser.ProtobufField f2 = fields.get(1);
        assertThat(f2.getNumber()).isEqualTo(2);
        assertThat(f2.getWireType()).isEqualTo(5);
        assertThat(f2.getValue()).isEqualTo(0x12345678);

        // Assert Field 3
        ProtobufParser.ProtobufField f3 = fields.get(2);
        assertThat(f3.getNumber()).isEqualTo(3);
        assertThat(f3.getWireType()).isEqualTo(1);
        assertThat(f3.getValue()).isEqualTo(0x1122334455667788L);

        // Assert Field 4
        ProtobufParser.ProtobufField f4 = fields.get(3);
        assertThat(f4.getNumber()).isEqualTo(4);
        assertThat(f4.getWireType()).isEqualTo(2);
        assertThat(f4.getValue()).isEqualTo("hello".getBytes());
    }
}
