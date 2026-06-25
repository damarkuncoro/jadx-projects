package dexforge.core.parser.native_lib;

import static org.assertj.core.api.Assertions.assertThat;

import dexforge.core.parser.native_lib.model.ElfSymbol;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

class ElfParserTest {

    @Test
    void testInvalidMagic() {
        byte[] invalidData = "invalid magic content".getBytes();
        ElfParser parser = new ElfParser(invalidData);
        List<ElfSymbol> symbols = parser.parseSymbols();

        assertThat(symbols).isEmpty();
    }

    @Test
    void testMinimal32BitElfHeader() {
        ByteBuffer buffer = ByteBuffer.allocate(64).order(ByteOrder.LITTLE_ENDIAN);
        buffer.put((byte) 0x7F);
        buffer.put((byte) 'E');
        buffer.put((byte) 'L');
        buffer.put((byte) 'F');
        buffer.put((byte) 1); // 32-bit

        // Fill up to offset 32 (Section Header Table Offset position)
        while (buffer.position() < 32) {
            buffer.put((byte) 0);
        }
        buffer.putInt(64); // shoff = 64

        // Fill up to offset 46 (shentsize, shnum, shstrndx position)
        while (buffer.position() < 46) {
            buffer.put((byte) 0);
        }
        buffer.putShort((short) 40); // shentsize
        buffer.putShort((short) 0);  // shnum
        buffer.putShort((short) 0);  // shstrndx

        ElfParser parser = new ElfParser(buffer.array());
        List<ElfSymbol> symbols = parser.parseSymbols();

        assertThat(symbols).isEmpty();
    }

    @Test
    void testMinimal64BitElfHeader() {
        ByteBuffer buffer = ByteBuffer.allocate(96).order(ByteOrder.LITTLE_ENDIAN);
        buffer.put((byte) 0x7F);
        buffer.put((byte) 'E');
        buffer.put((byte) 'L');
        buffer.put((byte) 'F');
        buffer.put((byte) 2); // 64-bit

        // Fill up to offset 40 (Section Header Table Offset position)
        while (buffer.position() < 40) {
            buffer.put((byte) 0);
        }
        buffer.putLong(96); // shoff = 96

        // Fill up to offset 58 (shentsize, shnum, shstrndx position)
        while (buffer.position() < 58) {
            buffer.put((byte) 0);
        }
        buffer.putShort((short) 64); // shentsize
        buffer.putShort((short) 0);  // shnum
        buffer.putShort((short) 0);  // shstrndx

        ElfParser parser = new ElfParser(buffer.array());
        List<ElfSymbol> symbols = parser.parseSymbols();

        assertThat(symbols).isEmpty();
    }
}
