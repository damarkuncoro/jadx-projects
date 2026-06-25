package dexforge.core.parser.arsc.parser;

import static org.assertj.core.api.Assertions.assertThat;

import dexforge.core.parser.arsc.model.ArscResourceValue;
import org.junit.jupiter.api.Test;

class ArscValueDecoderTest {

    @Test
    void testDecodeNullValue() {
        assertThat(ArscValueDecoder.decode(null)).isEqualTo("null");
    }

    @Test
    void testDecodeStandardTypes() {
        // String ref
        assertThat(ArscValueDecoder.decode(new ArscResourceValue(8, ArscResourceValue.TYPE_STRING, 42)))
                .isEqualTo("string_ref_42");

        // Decimal integer
        assertThat(ArscValueDecoder.decode(new ArscResourceValue(8, ArscResourceValue.TYPE_INT_DEC, 12345)))
                .isEqualTo("12345");

        // Hex integer
        assertThat(ArscValueDecoder.decode(new ArscResourceValue(8, ArscResourceValue.TYPE_INT_HEX, 0xABC)))
                .isEqualTo("0x00000abc");

        // Boolean
        assertThat(ArscValueDecoder.decode(new ArscResourceValue(8, ArscResourceValue.TYPE_INT_BOOLEAN, 1)))
                .isEqualTo("true");
        assertThat(ArscValueDecoder.decode(new ArscResourceValue(8, ArscResourceValue.TYPE_INT_BOOLEAN, 0)))
                .isEqualTo("false");
    }

    @Test
    void testDecodeColors() {
        // ARGB8
        assertThat(ArscValueDecoder.decode(new ArscResourceValue(8, ArscResourceValue.TYPE_INT_COLOR_ARGB8, 0xFF112233)))
                .isEqualTo("#FF112233");

        // RGB8
        assertThat(ArscValueDecoder.decode(new ArscResourceValue(8, ArscResourceValue.TYPE_INT_COLOR_RGB8, 0xFF112233)))
                .isEqualTo("#112233");

        // ARGB4
        assertThat(ArscValueDecoder.decode(new ArscResourceValue(8, ArscResourceValue.TYPE_INT_COLOR_ARGB4, 0x1234)))
                .isEqualTo("#1234");

        // RGB4
        assertThat(ArscValueDecoder.decode(new ArscResourceValue(8, ArscResourceValue.TYPE_INT_COLOR_RGB4, 0x1234)))
                .isEqualTo("#234");
    }

    @Test
    void testDecodeComplexTypes() {
        // Dimension: value = (complex & 0xFFFFFF00) >> 8, unit = index 1 ("dp")
        // complex = 100 << 8 | 1 = 25600 | 1 = 25601
        assertThat(ArscValueDecoder.decode(new ArscResourceValue(8, ArscResourceValue.TYPE_DIMENSION, 25601)))
                .isEqualTo("100.0dp");

        // Fraction: value = (complex & 0xFFFFFF00) >> 8, unit = index 0 ("%")
        // complex = 50 << 8 | 0 = 12800
        assertThat(ArscValueDecoder.decode(new ArscResourceValue(8, ArscResourceValue.TYPE_FRACTION, 12800)))
                .isEqualTo("50.0%");
    }

    @Test
    void testDecodeReferencesAndAttributes() {
        // Reference
        assertThat(ArscValueDecoder.decode(new ArscResourceValue(8, ArscResourceValue.TYPE_REFERENCE, 0x7f010001)))
                .isEqualTo("@0x7f010001");

        // Attribute
        assertThat(ArscValueDecoder.decode(new ArscResourceValue(8, ArscResourceValue.TYPE_ATTRIBUTE, 0x01010003)))
                .isEqualTo("?0x01010003");
    }
}
