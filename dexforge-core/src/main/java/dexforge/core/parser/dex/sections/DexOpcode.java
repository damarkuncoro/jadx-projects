package dexforge.core.parser.dex.sections;

/**
 * Dalvik Opcode definitions and utility methods.
 * Provides centralized logic for instruction categorization (DRY).
 */
public final class DexOpcode {

    public static boolean isConst(int opcode) {
        int op = opcode & 0xFF;
        return op >= 0x12 && op <= 0x1C;
    }

    public static boolean isString(int opcode) {
        int op = opcode & 0xFF;
        return op == 0x1A || op == 0x1B;
    }

    public static boolean isInvoke(int opcode) {
        int op = opcode & 0xFF;
        return op >= 0x6E && op <= 0x78;
    }

    public static boolean isFieldAccess(int opcode) {
        int op = opcode & 0xFF;
        return (op >= 0x44 && op <= 0x5F) || (op >= 0x60 && op <= 0x6D);
    }

    public static boolean isArithmetic(int opcode) {
        int op = opcode & 0xFF;
        // int/long/float/double add, sub, mul, div, rem
        return (op >= 0x90 && op <= 0x9B) || (op >= 0xAB && op <= 0xB5) ||
               (op >= 0xBA && op <= 0xC4) || (op >= 0xC9 && op <= 0xCF) ||
               (op >= 0xD8 && op <= 0xDF);
    }

    public static boolean isBitwise(int opcode) {
        int op = opcode & 0xFF;
        // and, or, xor, shl, shr, ushr
        return (op >= 0x9C && op <= 0xA1) || (op >= 0xB6 && op <= 0xBB) ||
               (op >= 0xC5 && op <= 0xC8) || (op >= 0x7B && op <= 0x8F);
    }

    public static boolean isComparison(int opcode) {
        int op = opcode & 0xFF;
        return (op >= 0x2D && op <= 0x31) || (op >= 0x32 && op <= 0x3D);
    }

    /**
     * Checks if the opcode writes to a destination register and returns its index.
     * Returns -1 if no destination register is written.
     */
    public static int getDestinationRegister(int rawOpcode) {
        int op = rawOpcode & 0xFF;

        // Format 12x: B|A|op (e.g., const/4, move, add-int/2addr, etc.)
        // 0x12 is const/4, 0x01 is move, 0x04 is move-wide, 0x07 is move-object
        // 0x90-0xAF are binary op/2addr
        if (op == 0x12 || op == 0x01 || op == 0x04 || op == 0x07 || (op >= 0x90 && op <= 0xAF)) {
            return (rawOpcode >> 8) & 0x0F;
        }

        // Format 22x, 32x, 11x, 21c, etc.: AA|op (AA is destination)
        // 0x13-0x15 are const/16, const, const/high16
        // 0x02, 0x03, 0x05, 0x06, 0x08, 0x09 are move variants
        // 0x0D is move-exception, 0x1F-0x20 check-cast, 0x22-0x23 new-instance/array
        // 0x44-0x51 aget/aput, 0x7B-0x8F unary, 0xD0-0xDF binary op/lit8
        if ((op >= 0x13 && op <= 0x15) || op == 0x02 || op == 0x03 || op == 0x05 || op == 0x06 ||
                op == 0x08 || op == 0x09 || op == 0x0D || (op >= 0x1F && op <= 0x20) ||
                (op >= 0x22 && op <= 0x23) || (op >= 0x44 && op <= 0x51) || (op >= 0x7B && op <= 0x8F) ||
                (op >= 0xD0 && op <= 0xDF)) {
            return (rawOpcode >> 8) & 0xFF;
        }

        return -1;
    }

    public static int getFormatLength(int opcode) {
        int op = opcode & 0xFF;
        // 5-unit opcodes
        if (op == 0x18) {
            return 5;
        }
        // 3-unit opcodes
        switch (op) {
            case 0x03: case 0x06: case 0x09:
            case 0x14: case 0x17: case 0x1B:
            case 0x24: case 0x25: case 0x26:
            case 0x2A: case 0x2B: case 0x2C:
            case 0x6E: case 0x6F: case 0x70: case 0x71: case 0x72:
            case 0x74: case 0x75: case 0x76: case 0x77: case 0x78:
                return 3;
        }
        // 1-unit opcodes
        if (op == 0x00 || op == 0x01 || op == 0x04 || op == 0x07) {
            return 1;
        }
        if (op >= 0x0A && op <= 0x12) {
            return 1;
        }
        if (op == 0x1D || op == 0x1E) {
            return 1;
        }
        if (op == 0x21 || op == 0x27 || op == 0x28) {
            return 1;
        }
        if (op >= 0x7B && op <= 0x8F) {
            return 1;
        }
        if (op >= 0xB0 && op <= 0xCF) {
            return 1;
        }
        // 2-unit opcodes (default)
        return 2;
    }

    public static String getMnemonic(int opcode) {
        int op = opcode & 0xFF;
        switch (op) {
            case 0x00: return "nop";
            case 0x01: return "move";
            case 0x02: return "move/from16";
            case 0x03: return "move/16";
            case 0x04: return "move-wide";
            case 0x05: return "move-wide/from16";
            case 0x06: return "move-wide/16";
            case 0x07: return "move-object";
            case 0x08: return "move-object/from16";
            case 0x09: return "move-object/16";
            case 0x0A: return "move-result";
            case 0x0B: return "move-result-wide";
            case 0x0C: return "move-result-object";
            case 0x0D: return "move-exception";
            case 0x0E: return "return-void";
            case 0x0F: return "return";
            case 0x10: return "return-wide";
            case 0x11: return "return-object";
            case 0x12: return "const/4";
            case 0x13: return "const/16";
            case 0x14: return "const";
            case 0x15: return "const/high16";
            case 0x1A: return "const-string";
            case 0x1B: return "const-string/jumbo";
            case 0x1C: return "const-class";
            case 0x22: return "new-instance";
            case 0x23: return "new-array";
            case 0x24: return "filled-new-array";
            case 0x25: return "filled-new-array/range";
            case 0x26: return "fill-array-data";
            case 0x28: return "goto";
            case 0x29: return "goto/16";
            case 0x2A: return "goto/32";
            case 0x32: return "if-eq";
            case 0x33: return "if-ne";
            case 0x34: return "if-lt";
            case 0x35: return "if-ge";
            case 0x36: return "if-gt";
            case 0x37: return "if-le";
            case 0x38: return "if-eqz";
            case 0x39: return "if-nez";
            case 0x44: return "iget";
            case 0x4B: return "iput";
            case 0x52: return "iget-object";
            case 0x59: return "iput-object";
            case 0x60: return "sget";
            case 0x62: return "sget-object";
            case 0x67: return "sput";
            case 0x69: return "sput-object";
            case 0x6E: return "invoke-virtual";
            case 0x6F: return "invoke-super";
            case 0x70: return "invoke-direct";
            case 0x71: return "invoke-static";
            case 0x72: return "invoke-interface";
            case 0x74: return "invoke-virtual/range";
            case 0x7B: return "neg-int";
            case 0x8D: return "int-to-byte";
            case 0x8F: return "int-to-char";
            case 0x90: return "add-int";
            case 0x91: return "sub-int";
            case 0x92: return "mul-int";
            case 0x94: return "rem-int";
            case 0x95: return "and-int";
            case 0x96: return "or-int";
            case 0x97: return "xor-int";
            case 0xB0: return "add-int/2addr";
            case 0xB1: return "sub-int/2addr";
            case 0xB2: return "mul-int/2addr";
            case 0xB5: return "and-int/2addr";
            case 0xB6: return "or-int/2addr";
            case 0xB7: return "xor-int/2addr";
            case 0xD0: return "add-int/lit16";
            case 0xD8: return "add-int/lit8";
            case 0xD9: return "rsub-int/lit8";
            case 0xDA: return "mul-int/lit8";
            case 0xDD: return "and-int/lit8";
            case 0xDE: return "or-int/lit8";
            case 0xDF: return "xor-int/lit8";
            case 0xE0: return "shl-int/lit8";
            case 0xE1: return "shr-int/lit8";
            case 0xE2: return "ushr-int/lit8";
            default: return String.format("op_0x%02x", op);
        }
    }
}
