package dexforge.core.parser.native_lib;

import dexforge.core.parser.dex.io.DexByteReader;
import dexforge.core.parser.native_lib.model.ElfSymbol;
import java.util.ArrayList;
import java.util.List;

/**
 * Enhanced parser for ELF (.so) files.
 * Extracts exported symbols and identifies JNI bridges.
 */
public final class ElfParser {
    private final DexByteReader reader;
    private boolean is64Bit;

    public ElfParser(byte[] data) {
        this.reader = new DexByteReader(data);
    }

    public List<ElfSymbol> parseSymbols() {
        List<ElfSymbol> symbols = new ArrayList<>();

        try {
            // 1. Verify Magic
            reader.setPosition(0);
            if (reader.readByte() != 0x7F || reader.readByte() != 'E' ||
                reader.readByte() != 'L' || reader.readByte() != 'F') {
                return symbols;
            }

            // 2. Class (32/64 bit)
            int eiClass = reader.readUbyte();
            is64Bit = (eiClass == 2);

            // 3. Section Header Table Offset
            reader.setPosition(is64Bit ? 40 : 32);
            long shoff = is64Bit ? reader.readLong() : reader.readUint();

            // 4. Section Header Info
            reader.setPosition(is64Bit ? 58 : 46);
            int shentsize = reader.readUshort();
            int shnum = reader.readUshort();
            int shstrndx = reader.readUshort();

            // 5. Find .dynsym and .dynstr sections
            long dynsymOff = 0, dynsymSize = 0, dynsymEntSize = 0;
            long dynstrOff = 0;

            for (int i = 0; i < shnum; i++) {
                reader.setPosition((int) (shoff + (i * shentsize)));
                int shName = reader.readInt();
                int shType = reader.readInt();

                // shType 11 = SHT_DYNSYM, 3 = SHT_STRTAB
                if (shType == 11) {
                    reader.setPosition((int) (shoff + (i * shentsize) + (is64Bit ? 24 : 16)));
                    dynsymOff = is64Bit ? reader.readLong() : reader.readUint();
                    dynsymSize = is64Bit ? reader.readLong() : reader.readUint();
                    reader.setPosition((int) (shoff + (i * shentsize) + (is64Bit ? 56 : 32)));
                    dynsymEntSize = is64Bit ? reader.readLong() : reader.readUint();
                } else if (shType == 3 && i != shstrndx) {
                    // This is a heuristic to find the string table for symbols
                    reader.setPosition((int) (shoff + (i * shentsize) + (is64Bit ? 24 : 16)));
                    dynstrOff = is64Bit ? reader.readLong() : reader.readUint();
                }
            }

            // 6. Read Symbols
            if (dynsymOff != 0 && dynstrOff != 0 && dynsymEntSize > 0) {
                int count = (int) (dynsymSize / dynsymEntSize);
                for (int i = 0; i < count; i++) {
                    reader.setPosition((int) (dynsymOff + (i * dynsymEntSize)));
                    int stName = reader.readInt();

                    // Skip if name index is 0
                    if (stName == 0) continue;

                    // Read info byte to check visibility/binding
                    reader.setPosition((int) (dynsymOff + (i * dynsymEntSize) + (is64Bit ? 4 : 12)));
                    int stInfo = reader.readUbyte();
                    int bind = stInfo >> 4; // 1 = GLOBAL

                    if (bind == 1) {
                        // Extract name from dynstr
                        int currentPos = reader.position();
                        reader.setPosition((int) (dynstrOff + stName));
                        String name = readNullTerminatedString();
                        reader.setPosition(currentPos);

                        if (name.startsWith("Java_")) {
                            symbols.add(new ElfSymbol(name, 0, true));
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Parse error
        }

        return symbols;
    }

    private String readNullTerminatedString() {
        StringBuilder sb = new StringBuilder();
        byte b;
        while ((b = reader.readByte()) != 0) {
            sb.append((char) b);
        }
        return sb.toString();
    }
}
