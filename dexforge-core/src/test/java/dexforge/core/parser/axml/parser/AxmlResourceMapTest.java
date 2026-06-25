package dexforge.core.parser.axml.parser;

import static org.assertj.core.api.Assertions.assertThat;

import dexforge.core.parser.arsc.io.ArscReader;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

class AxmlResourceMapTest {

    @Test
    void testParseResourceMap() {
        ByteBuffer buffer = ByteBuffer.allocate(16).order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(0x00080180);      // Type
        buffer.putInt(16);              // Size
        buffer.putInt(0x7f010001);      // Res ID 0
        buffer.putInt(0x7f010002);      // Res ID 1

        ArscReader reader = new ArscReader(buffer.array());
        AxmlResourceMap resourceMap = new AxmlResourceMap(reader, 0);

        assertThat(resourceMap.getResourceId(0)).isEqualTo(0x7f010001);
        assertThat(resourceMap.getResourceId(1)).isEqualTo(0x7f010002);
        assertThat(resourceMap.getResourceId(2)).isEqualTo(0);

        assertThat(resourceMap.getMappings()).hasSize(2);
        assertThat(resourceMap.getMappings()).containsEntry(0, 0x7f010001);
        assertThat(resourceMap.getMappings()).containsEntry(1, 0x7f010002);
    }
}
