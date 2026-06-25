package dexforge.core.parser.dex.sections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dexforge.core.parser.dex.io.DexByteReader;
import dexforge.core.parser.dex.io.Leb128;

/**
 * Parser for debug_info_item in DEX files.
 * Primarily used to extract line number mappings and local variable info.
 */
public final class DexDebugInfoParser {
    private final DexByteReader reader;
    private final DexStringPool stringPool;
    private final DexTypePool typePool;

    public DexDebugInfoParser(DexByteReader reader, DexStringPool stringPool, DexTypePool typePool) {
        this.reader = reader;
        this.stringPool = stringPool;
        this.typePool = typePool;
    }

    public static final class DebugInfo {
        public final Map<Integer, Integer> lineNumbers;
        public final List<LocalVar> localVars;

        public DebugInfo(Map<Integer, Integer> lineNumbers, List<LocalVar> localVars) {
            this.lineNumbers = lineNumbers;
            this.localVars = localVars;
        }
    }

    public static final class LocalVar {
        public final int reg;
        public final String name;
        public final String type;
        public final int startAddress;
        public int endAddress;

        public LocalVar(int reg, String name, String type, int startAddress) {
            this.reg = reg;
            this.name = name;
            this.type = type;
            this.startAddress = startAddress;
        }
    }

    /**
     * Parses the debug info for a method.
     */
    public DebugInfo parse(int debugInfoOff) {
        if (debugInfoOff == 0) return new DebugInfo(new HashMap<>(), new ArrayList<>());

        DexByteReader debugReader = reader.at(debugInfoOff);
        int line = Leb128.readUleb128(debugReader);
        int parametersSize = Leb128.readUleb128(debugReader);

        for (int i = 0; i < parametersSize; i++) {
            Leb128.readUleb128(debugReader);
        }

        Map<Integer, Integer> lineNumbers = new HashMap<>();
        List<LocalVar> localVars = new ArrayList<>();
        Map<Integer, LocalVar> activeLocals = new HashMap<>();
        int address = 0;

        while (true) {
            int opcode = debugReader.readUbyte();
            switch (opcode) {
                case 0x00: // DBG_END_SEQUENCE
                    for (LocalVar lv : activeLocals.values()) {
                        lv.endAddress = address;
                    }
                    return new DebugInfo(lineNumbers, localVars);

                case 0x01: // DBG_ADVANCE_PC
                    address += Leb128.readUleb128(debugReader);
                    break;

                case 0x02: // DBG_ADVANCE_LINE
                    line += Leb128.readSleb128(debugReader);
                    break;

                case 0x03: // DBG_START_LOCAL
                case 0x04: // DBG_START_LOCAL_EXTENDED
                {
                    int reg = Leb128.readUleb128(debugReader);
                    int nameIdx = Leb128.readUleb128(debugReader) - 1;
                    int typeIdx = Leb128.readUleb128(debugReader) - 1;
                    if (opcode == 0x04) Leb128.readUleb128(debugReader);

                    String name = (nameIdx >= 0) ? stringPool.getString(nameIdx) : "v" + reg;
                    String type = (typeIdx >= 0) ? typePool.getTypeName(typeIdx) : "Object";

                    LocalVar lv = new LocalVar(reg, name, type, address);
                    localVars.add(lv);
                    activeLocals.put(reg, lv);
                    break;
                }

                case 0x05: // DBG_END_LOCAL
                {
                    int reg = Leb128.readUleb128(debugReader);
                    LocalVar lv = activeLocals.remove(reg);
                    if (lv != null) lv.endAddress = address;
                    break;
                }

                case 0x06: // DBG_RESTART_LOCAL
                {
                    int reg = Leb128.readUleb128(debugReader);
                    // Simplified: just mark as new local for now
                    break;
                }

                // ... (skipping prologue/epilogue/file for brevity)
                case 0x07: case 0x08: case 0x09:
                    if (opcode == 0x09) Leb128.readUleb128(debugReader);
                    break;

                default: {
                    int adjOpcode = opcode - 0x0a;
                    address += adjOpcode / 15;
                    line += -4 + (adjOpcode % 15);
                    lineNumbers.put(address, line);
                    break;
                }
            }
        }
    }
}
