package dexforge.core.parser.assets;

import dexforge.core.parser.dex.io.DexByteReader;
import dexforge.core.parser.dex.io.Leb128;
import java.util.ArrayList;
import java.util.List;

/**
 * Basic Protobuf decoder for assets.
 * Decodes wire-format data without a schema (Schema-less decoding).
 */
public final class ProtobufParser {
    private final DexByteReader reader;

    public ProtobufParser(byte[] data) {
        this.reader = new DexByteReader(data);
    }

    public List<ProtobufField> parse() {
        List<ProtobufField> fields = new ArrayList<>();
        while (reader.position() < reader.limit()) {
            int tag = Leb128.readUleb128(reader);
            int wireType = tag & 0x07;
            int fieldNumber = tag >>> 3;

            Object value = null;
            switch (wireType) {
                case 0: // Varint
                    value = Leb128.readUleb128(reader);
                    break;
                case 1: // 64-bit
                    value = reader.readLong();
                    break;
                case 2: // Length-delimited
                    int length = Leb128.readUleb128(reader);
                    value = reader.readByteArray(length);
                    break;
                case 5: // 32-bit
                    value = reader.readInt();
                    break;
            }
            fields.add(new ProtobufField(fieldNumber, wireType, value));
        }
        return fields;
    }

    public static final class ProtobufField {
        private final int number;
        private final int wireType;
        private final Object value;

        public ProtobufField(int number, int wireType, Object value) {
            this.number = number;
            this.wireType = wireType;
            this.value = value;
        }

        public int getNumber() { return number; }
        public int getWireType() { return wireType; }
        public Object getValue() { return value; }
    }
}
