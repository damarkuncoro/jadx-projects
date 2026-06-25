package dexforge.core.parser.dex.sections;

import dexforge.core.parser.dex.io.DexByteReader;
import dexforge.core.parser.dex.model.DexCode;

/**
 * Parser for the code_item section.
 */
public final class DexCodeParser {
    private final DexByteReader reader;

    public DexCodeParser(DexByteReader reader) {
        this.reader = reader;
    }

    public DexCode parse(int offset) {
        if (offset == 0) return null;

        DexByteReader codeReader = reader.at(offset);
        int registersSize = codeReader.readUshort();
        int insSize = codeReader.readUshort();
        int outsSize = codeReader.readUshort();
        int triesSize = codeReader.readUshort();
        int debugInfoOff = codeReader.readInt();
        int insnsSize = codeReader.readInt();

        short[] instructions = new short[insnsSize];
        for (int i = 0; i < insnsSize; i++) {
            instructions[i] = codeReader.readShort();
        }

        return new DexCode(registersSize, insSize, outsSize, debugInfoOff, instructions, triesSize);
    }
}
