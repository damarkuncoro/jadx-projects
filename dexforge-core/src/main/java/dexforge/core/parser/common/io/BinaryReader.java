package dexforge.core.parser.common.io;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * REUSEABLE: Base binary reader for all Android formats (DEX, ARSC, AXML).
 * Follows SOLID by providing a unified interface for binary access.
 */
public class BinaryReader {
    protected final ByteBuffer buffer;

    public BinaryReader(byte[] data) {
        this(ByteBuffer.wrap(data));
    }

    public BinaryReader(ByteBuffer buffer) {
        this.buffer = buffer.order(ByteOrder.LITTLE_ENDIAN);
    }

    public int position() { return buffer.position(); }
    public void setPosition(int pos) { buffer.position(pos); }
    public int limit() { return buffer.limit(); }

    public byte readByte() { return buffer.get(); }
    public int readUbyte() { return buffer.get() & 0xFF; }
    public short readShort() { return buffer.getShort(); }
    public int readUshort() { return buffer.getShort() & 0xFFFF; }
    public int readInt() { return buffer.getInt(); }
    public long readUint() { return buffer.getInt() & 0xFFFFFFFFL; }
    public long readLong() { return buffer.getLong(); }

    public byte[] readByteArray(int length) {
        byte[] data = new byte[length];
        buffer.get(data);
        return data;
    }

    public String readUtf16String(int charCount) {
        byte[] data = readByteArray(charCount * 2);
        return new String(data, java.nio.charset.StandardCharsets.UTF_16LE).trim();
    }
}
