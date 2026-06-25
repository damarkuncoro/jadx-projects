package dexforge.core.parser.arsc.io;

import dexforge.core.parser.common.io.BinaryReader;
import java.nio.ByteBuffer;

/**
 * Specialized ARSC reader extending the common BinaryReader.
 * REUSEABLE & DRY.
 */
public final class ArscReader extends BinaryReader {

    public ArscReader(byte[] data) {
        super(data);
    }

    public ArscReader(ByteBuffer buffer) {
        super(buffer);
    }

    public String readString(int size) {
        byte[] data = readByteArray(size);
        int len = 0;
        while (len < data.length && data[len] != 0) len++;
        return new String(data, 0, len);
    }
}
