package dexforge.core.parser.dex.sections;

import java.util.ArrayList;
import java.util.List;

import dexforge.core.parser.dex.model.DexCode;
import dexforge.core.parser.dex.model.DexInstruction;

/**
 * Enhanced decoder for Dalvik instructions.
 * Decodes all instructions to support CFG building and full disassembled output.
 */
public final class DexInstructionDecoder {

    public static List<DexInstruction> decode(DexCode code) {
        if (code == null) return java.util.Collections.emptyList();

        short[] insns = code.getInstructions();
        List<DexInstruction> results = new ArrayList<>();
        int i = 0;
        while (i < insns.length) {
            int rawOpcode = insns[i] & 0xFFFF;
            int opcode = rawOpcode & 0xFF;
            int length = DexOpcode.getFormatLength(opcode);

            // Extract reference index or jump target if applicable
            int index = -1;
            if (opcode == 0x1A) { // CONST_STRING
                if (i + 1 < insns.length) index = insns[i + 1] & 0xFFFF;
            } else if (opcode == 0x1B) { // CONST_STRING_JUMBO
                if (i + 2 < insns.length) {
                    index = (insns[i + 1] & 0xFFFF) | ((insns[i + 2] & 0xFFFF) << 16);
                }
            } else if (opcode >= 0x6E && opcode <= 0x78) { // INVOKE-KIND
                if (i + 1 < insns.length) index = insns[i + 1] & 0xFFFF;
            } else if (opcode >= 0x52 && opcode <= 0x66) { // IGET/IPUT/SGET/SPUT
                if (i + 1 < insns.length) index = insns[i + 1] & 0xFFFF;
            } else if (opcode >= 0x28 && opcode <= 0x2A) { // GOTO
                // Simple target extraction for demo
                index = rawOpcode >> 8;
            } else if (opcode >= 0x32 && opcode <= 0x3D) { // IF-*
                if (i + 1 < insns.length) index = insns[i + 1];
            } else if (opcode == 0x1F || opcode == 0x20 || opcode == 0x22 || opcode == 0x23) {
                if (i + 1 < insns.length) index = insns[i + 1] & 0xFFFF;
            }

            short[] units = null;
            if (length > 0 && i + length <= insns.length) {
                units = new short[length];
                System.arraycopy(insns, i, units, 0, length);
            }

            // Always add instruction now
            DexInstruction insnObj = new DexInstruction(rawOpcode, index, i, length, units);
            decodeOperands(insnObj, insns, i);
            results.add(insnObj);

            if (length <= 0) break;
            i += length;
        }
        return results;
    }

    private static void decodeOperands(DexInstruction insn, short[] insns, int i) {
        int opcode = insn.getOpcode() & 0xFF;

        // Format 11n: B|A|op (const/4)
        if (opcode == 0x12) {
            insn.setRegisters(new int[]{(insns[i] >> 8) & 0x0F});
            long lit = (insns[i] >> 12) & 0x0F;
            if ((lit & 0x08) != 0) lit |= 0xFFFFFFFFFFFFFFF0L;
            insn.setLiteral(lit);
            return;
        }

        // Format 21c/21t/21h: AA|op BBBB (const/16, const/high16, const-string, const-class, sget/sput, if-testz)
        if (opcode == 0x13 || opcode == 0x15 || opcode == 0x1A || opcode == 0x1C ||
                opcode == 0x22 || (opcode >= 0x60 && opcode <= 0x6D) || (opcode >= 0x38 && opcode <= 0x3D)) {
            if (i + 1 >= insns.length) return;
            insn.setRegisters(new int[]{(insns[i] >> 8) & 0xFF});
            if (opcode == 0x15) insn.setLiteral((long) insns[i + 1] << 16);
            else if (opcode == 0x13) insn.setLiteral(insns[i + 1]);
            return;
        }

        // Format 31c: AA|op BBBBBBBB (const-string/jumbo)
        if (opcode == 0x1B) {
            if (i + 2 >= insns.length) return;
            insn.setRegisters(new int[]{(insns[i] >> 8) & 0xFF});
            return;
        }

        // Format 31t: AA|op BBBBBBBB (fill-array-data)
        if (opcode == 0x26) {
            if (i + 2 >= insns.length) return;
            insn.setRegisters(new int[]{(insns[i] >> 8) & 0xFF});
            int offset = (insns[i + 1] & 0xFFFF) | ((insns[i + 2] & 0xFFFF) << 16);
            insn.setLiteral(offset);
            return;
        }

        // Format 22c/22t/22s: B|A|op CCCC (new-array, instance-of, iget/iput, if-test, binop/lit16)
        if (opcode == 0x20 || opcode == 0x23 || (opcode >= 0x52 && opcode <= 0x5F) ||
                (opcode >= 0x32 && opcode <= 0x37) || (opcode >= 0xD0 && opcode <= 0xD7)) {
            if (i + 1 >= insns.length) return;
            insn.setRegisters(new int[]{(insns[i] >> 8) & 0x0F, (insns[i] >> 12) & 0x0F});
            if (opcode >= 0xD0 && opcode <= 0xD7) insn.setLiteral(insns[i + 1]);
            return;
        }

        // Format 23x: AA|op CC|BB (binop, aget/aput)
        if ((opcode >= 0x90 && opcode <= 0xAF) || (opcode >= 0x44 && opcode <= 0x51)) {
            if (i + 1 >= insns.length) return;
            insn.setRegisters(new int[]{(insns[i] >> 8) & 0xFF, insns[i + 1] & 0xFF, (insns[i + 1] >> 8) & 0xFF});
            return;
        }

        // Format 22b: AA|op CC|BB (binop/lit8)
        if (opcode >= 0xD8 && opcode <= 0xE2) {
            if (i + 1 >= insns.length) return;
            insn.setRegisters(new int[]{(insns[i] >> 8) & 0xFF, insns[i + 1] & 0xFF});
            long lit = (insns[i + 1] >> 8) & 0xFF;
            if ((lit & 0x80) != 0) lit |= 0xFFFFFFFFFFFFFF00L;
            insn.setLiteral(lit);
            return;
        }

        // Format 12x: B|A|op (move, binop/2addr)
        if ((opcode >= 0x01 && opcode <= 0x09) || (opcode >= 0xB0 && opcode <= 0xCF)) {
            insn.setRegisters(new int[]{(insns[i] >> 8) & 0x0F, (insns[i] >> 12) & 0x0F});
            return;
        }

        // Format 11x: AA|op
        if ((opcode >= 0x0A && opcode <= 0x11) || opcode == 0x1F || opcode == 0x27) {
            insn.setRegisters(new int[]{(insns[i] >> 8) & 0xFF});
            return;
        }

        // Format 35c: A|G|op BBBB F|E|D|C (invoke-kind)
        if (opcode >= 0x6E && opcode <= 0x72) {
            if (i + 2 >= insns.length) return;
            int count = (insns[i] >> 12) & 0x0F;
            int[] regs = new int[count];
            if (count > 0) regs[0] = insns[i + 2] & 0x0F;
            if (count > 1) regs[1] = (insns[i + 2] >> 4) & 0x0F;
            if (count > 2) regs[2] = (insns[i + 2] >> 8) & 0x0F;
            if (count > 3) regs[3] = (insns[i + 2] >> 12) & 0x0F;
            if (count > 4) regs[4] = (insns[i] >> 8) & 0x0F;
            insn.setRegisters(regs);
            return;
        }
    }
}
