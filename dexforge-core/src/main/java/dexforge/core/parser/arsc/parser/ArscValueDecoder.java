package dexforge.core.parser.arsc.parser;

import dexforge.core.parser.arsc.model.ArscResourceValue;

/**
 * Decodes Android Resource values into human-readable strings (e.g., colors, dimensions).
 */
public final class ArscValueDecoder {

    private static final String[] DIMENSION_UNITS = {"px", "dp", "sp", "pt", "in", "mm"};
    private static final String[] FRACTION_UNITS = {"%", "%p"};

    public static String decode(ArscResourceValue value) {
        if (value == null) return "null";

        int type = value.getType();
        int data = value.getData();

        switch (type) {
            case ArscResourceValue.TYPE_STRING:
                return "string_ref_" + data; // Handled by StringPool usually
            case ArscResourceValue.TYPE_INT_DEC:
                return String.valueOf(data);
            case ArscResourceValue.TYPE_INT_HEX:
                return String.format("0x%08x", data);
            case ArscResourceValue.TYPE_INT_BOOLEAN:
                return data != 0 ? "true" : "false";
            case ArscResourceValue.TYPE_INT_COLOR_ARGB8:
                return String.format("#%08X", data);
            case ArscResourceValue.TYPE_INT_COLOR_RGB8:
                return String.format("#%06X", data & 0xFFFFFF);
            case ArscResourceValue.TYPE_INT_COLOR_ARGB4:
                return String.format("#%04X", data & 0xFFFF);
            case ArscResourceValue.TYPE_INT_COLOR_RGB4:
                return String.format("#%03X", data & 0xFFF);
            case ArscResourceValue.TYPE_DIMENSION:
                return decodeComplex(data, DIMENSION_UNITS);
            case ArscResourceValue.TYPE_FRACTION:
                return decodeComplex(data, FRACTION_UNITS);
            case ArscResourceValue.TYPE_REFERENCE:
                return String.format("@0x%08x", data);
            case ArscResourceValue.TYPE_ATTRIBUTE:
                return String.format("?0x%08x", data);
            default:
                return String.format("val(0x%02x, 0x%08x)", type, data);
        }
    }

    private static String decodeComplex(int complex, String[] units) {
        float value = (complex & 0xFFFFFF00) >> 8; // simplified
        // In real Android, this uses complexToFloat which handles radix shifts
        int unitIdx = complex & 0x0F;
        String unit = (unitIdx >= 0 && unitIdx < units.length) ? units[unitIdx] : "";
        return value + unit;
    }
}
