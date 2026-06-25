package dexforge.core.parser.arsc.model;

public final class ArscResourceValue {
    public static final int TYPE_NULL = 0x00;
    public static final int TYPE_REFERENCE = 0x01;
    public static final int TYPE_ATTRIBUTE = 0x02;
    public static final int TYPE_STRING = 0x03;
    public static final int TYPE_FLOAT = 0x04;
    public static final int TYPE_DIMENSION = 0x05;
    public static final int TYPE_FRACTION = 0x06;
    public static final int TYPE_DYNAMIC_REFERENCE = 0x07;
    public static final int TYPE_DYNAMIC_ATTRIBUTE = 0x08;
    public static final int TYPE_INT_DEC = 0x10;
    public static final int TYPE_INT_HEX = 0x11;
    public static final int TYPE_INT_BOOLEAN = 0x12;
    public static final int TYPE_INT_COLOR_ARGB8 = 0x1c;
    public static final int TYPE_INT_COLOR_RGB8 = 0x1d;
    public static final int TYPE_INT_COLOR_ARGB4 = 0x1e;
    public static final int TYPE_INT_COLOR_RGB4 = 0x1f;

    private final int size;
    private final int type;
    private final int data;

    public ArscResourceValue(int size, int type, int data) {
        this.size = size;
        this.type = type;
        this.data = data;
    }

    public int getSize() { return size; }
    public int getType() { return type; }
    public int getData() { return data; }

    @Override
    public String toString() {
        return String.format("Value[type=0x%02x, data=0x%08x]", type, data);
    }
}
