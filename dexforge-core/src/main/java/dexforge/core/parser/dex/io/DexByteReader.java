package dexforge.core.parser.dex.io;

import dexforge.core.parser.common.io.BinaryReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Specialized DEX reader extending the common BinaryReader.
 * DRY: Inherits standard binary operations.
 */
public final class DexByteReader extends BinaryReader {

    public DexByteReader(byte[] data) {
        super(data);
    }

    public DexByteReader(ByteBuffer buffer) {
        super(buffer);
    }

    public DexByteReader copy() {
        return new DexByteReader(buffer.duplicate().order(ByteOrder.LITTLE_ENDIAN));
    }

    public DexByteReader at(int offset) {
        DexByteReader copy = copy();
        copy.setPosition(offset);
        return copy;
    }
}
