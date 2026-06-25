package dexforge.core.parser.dex.io;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Utility for writing binary data in DEX format.
 */
public final class DexByteWriter {
    private ByteBuffer buffer;

    public DexByteWriter(int initialCapacity) {
        this.buffer = ByteBuffer.allocate(initialCapacity).order(ByteOrder.LITTLE_ENDIAN);
    }

    private void ensureCapacity(int needed) {
        if (buffer.remaining() < needed) {
            ByteBuffer newBuffer = ByteBuffer.allocate(buffer.capacity() * 2 + needed).order(ByteOrder.LITTLE_ENDIAN);
            buffer.flip();
            newBuffer.put(buffer);
            buffer = newBuffer;
        }
    }

    public void writeByte(int b) {
        ensureCapacity(1);
        buffer.put((byte) b);
    }

    public void writeShort(int s) {
        ensureCapacity(2);
        buffer.putShort((short) s);
    }

    public void writeInt(int i) {
        ensureCapacity(4);
        buffer.putInt(i);
    }

    public void writeUint(long l) {
        writeInt((int) l);
    }

    public void writeUleb128(int value) {
        while ((value & 0xffffff80) != 0) {
            writeByte((value & 0x7f) | 0x80);
            value >>>= 7;
        }
        writeByte(value & 0x7f);
    }

    public void writeByteArray(byte[] data) {
        ensureCapacity(data.length);
        buffer.put(data);
    }

    public byte[] toByteArray() {
        byte[] result = new byte[buffer.position()];
        buffer.flip();
        buffer.get(result);
        buffer.flip(); // restore for further writing if needed
        return result;
    }

    public int position() {
        return buffer.position();
    }

    public void setPosition(int pos) {
        buffer.position(pos);
    }
}
