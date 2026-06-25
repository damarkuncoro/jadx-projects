package dexforge.core.parser.analysis.emulator;

import dexforge.core.parser.dex.model.DexInstruction;
import dexforge.core.parser.dex.service.DexFastIndexer;
import dexforge.core.parser.dex.sections.DexStringPool;
import dexforge.core.parser.dex.sections.DexTypePool;
import dexforge.core.parser.dex.sections.DexFieldPool;
import dexforge.core.parser.dex.sections.DexMethodPool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for InstructionSet.
 *
 * Covers:
 *  - MOVE family (0x01–0x09, 0x0D)
 *  - MOVE_RESULT family (0x0A–0x0C) — verifikasi tidak di-overwrite move
 *  - CONST family (0x12–0x15, 0x1A, 0x1C)
 *  - NEW_INSTANCE (0x22)
 *  - ARITHMETIC: 23x (0x90–0x97), 2addr (0xB0–0xB7), lit8 (0xD8–0xE1)
 *  - ARRAY: array-length (0x21), new-array (0x23), aget/aput (0x44–0x51)
 *  - FIELD ACCESS: iget/iput (0x52–0x5F), sget/sput (0x60–0x6D)
 *  - CASTS (0x81, 0x8D, 0x8E, 0x8F)
 *  - BUG REGRESSIONS dari review
 */
@DisplayName("InstructionSet")
class InstructionSetTest {

    // ── Mocks ────────────────────────────────────────────────────────────────

    private EmulatorState state;
    private DexFastIndexer indexer;
    private InstructionSet instructionSet;
    private Map<Integer, Object> regs;
    private MethodInvokeHandler invokeHandlerMock;

    @BeforeEach
    void setUp() {
        state   = mock(EmulatorState.class);
        indexer = mock(DexFastIndexer.class);
        regs    = new HashMap<>();
        invokeHandlerMock = mock(MethodInvokeHandler.class);

        // Default indexer stubs
        DexStringPool sp = mock(DexStringPool.class);
        DexTypePool   tp = mock(DexTypePool.class);
        DexFieldPool  fp = mock(DexFieldPool.class);

        when(indexer.getStringPool()).thenReturn(sp);
        when(indexer.getTypePool()).thenReturn(tp);
        when(indexer.getFieldPool()).thenReturn(fp);

        when(sp.getSize()).thenReturn(100);
        when(tp.getSize()).thenReturn(100);
        when(state.getRegisters()).thenReturn(regs);

        instructionSet = new InstructionSet(state, indexer, invokeHandlerMock);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Buat DexInstruction dengan opcode, units, registers, literal, index. */
    private DexInstruction insn(int opcode, short[] units, int[] registers, long literal, int index) {
        DexInstruction i = mock(DexInstruction.class);
        when(i.getOpcode()).thenReturn(opcode);
        when(i.getUnits()).thenReturn(units);
        when(i.getRegisters()).thenReturn(registers);
        when(i.getLiteral()).thenReturn(literal);
        when(i.getIndex()).thenReturn(index);
        return i;
    }

    private void execute(int opcode, DexInstruction i) {
        InstructionExecutor exec = instructionSet.getExecutor(opcode);
        assertNotNull(exec, "Tidak ada executor untuk opcode 0x" + Integer.toHexString(opcode));
        exec.execute(i, regs);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // MOVE FAMILY
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("MOVE family (0x01–0x09, 0x0D)")
    class MoveFamily {

        @ParameterizedTest(name = "opcode=0x{0}")
        @ValueSource(ints = {0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0D})
        @DisplayName("menyalin nilai dari register sumber ke tujuan")
        void moveCopiesValue(int opcode) {
            regs.put(1, 42);
            DexInstruction i = insn(opcode, null, new int[]{0, 1}, 0, -1);
            execute(opcode, i);
            assertEquals(42, regs.get(0));
        }

        @Test
        @DisplayName("move tidak crash jika registers null")
        void moveNullSafe() {
            DexInstruction i = insn(0x01, null, null, 0, -1);
            assertDoesNotThrow(() -> execute(0x01, i));
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // MOVE-RESULT FAMILY — BUG #1 regression
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("MOVE-RESULT family (0x0A–0x0C) — BUG #1 regression")
    class MoveResultFamily {

        @ParameterizedTest(name = "opcode=0x{0}")
        @ValueSource(ints = {0x0A, 0x0B, 0x0C})
        @DisplayName("harus membaca lastResult, bukan register lain")
        void moveResultReadsLastResult(int opcode) {
            when(state.getLastResult()).thenReturn("expected_result");
            // register[1] berisi nilai berbeda — seharusnya TIDAK dipakai
            regs.put(1, "wrong_value");
            DexInstruction i = insn(opcode, null, new int[]{0, 1}, 0, -1);
            execute(opcode, i);
            assertEquals("expected_result", regs.get(0),
                "Opcode 0x" + Integer.toHexString(opcode) + " harus pakai lastResult, bukan move biasa");
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // CONST FAMILY
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("CONST family (0x12–0x15)")
    class ConstFamily {

        @ParameterizedTest(name = "opcode=0x{0}")
        @ValueSource(ints = {0x12, 0x13, 0x14, 0x15})
        @DisplayName("menaruh literal ke register tujuan")
        void constLoadsLiteral(int opcode) {
            DexInstruction i = insn(opcode, null, new int[]{2}, 99L, -1);
            execute(opcode, i);
            assertEquals(99, regs.get(2));
        }
    }

    @Nested
    @DisplayName("CONST-STRING (0x1A)")
    class ConstString {

        @Test
        @DisplayName("menaruh string dari pool ke register")
        void constStringLoadsString() {
            when(indexer.getStringPool().getSize()).thenReturn(10);
            when(indexer.getStringPool().getString(3)).thenReturn("hello");
            DexInstruction i = insn(0x1A, null, new int[]{0}, 0, 3);
            execute(0x1A, i);
            assertEquals("hello", regs.get(0));
        }

        @Test
        @DisplayName("index di luar pool menghasilkan null, tidak throw")
        void constStringOutOfBounds() {
            when(indexer.getStringPool().getSize()).thenReturn(2);
            DexInstruction i = insn(0x1A, null, new int[]{0}, 0, 99);
            assertDoesNotThrow(() -> execute(0x1A, i));
            assertNull(regs.get(0));
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // ARITHMETIC 23x
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("ARITHMETIC 23x (0x90–0x97)")
    class Arithmetic23x {

        /** Buat insn format 23x: unit[0]=opcode|regA<<8, unit[1]=regB|regC<<8 */
        private DexInstruction arithInsn(int opcode, int regA, int regB, int regC) {
            short[] units = {
                (short) ((opcode & 0xFF) | (regA << 8)),
                (short) ((regB & 0xFF)  | (regC << 8))
            };
            return insn(opcode, units, null, 0, -1);
        }

        @Test @DisplayName("add-int (0x90)") void addInt() {
            regs.put(1, 10); regs.put(2, 3);
            execute(0x90, arithInsn(0x90, 0, 1, 2));
            assertEquals(13, regs.get(0));
        }

        @Test @DisplayName("sub-int (0x91)") void subInt() {
            regs.put(1, 10); regs.put(2, 3);
            execute(0x91, arithInsn(0x91, 0, 1, 2));
            assertEquals(7, regs.get(0));
        }

        @Test @DisplayName("mul-int (0x92)") void mulInt() {
            regs.put(1, 6); regs.put(2, 7);
            execute(0x92, arithInsn(0x92, 0, 1, 2));
            assertEquals(42, regs.get(0));
        }

        @Test @DisplayName("rem-int (0x94)") void remInt() {
            regs.put(1, 10); regs.put(2, 3);
            execute(0x94, arithInsn(0x94, 0, 1, 2));
            assertEquals(1, regs.get(0));
        }

        @Test @DisplayName("and-int (0x95)") void andInt() {
            regs.put(1, 0b1010); regs.put(2, 0b1100);
            execute(0x95, arithInsn(0x95, 0, 1, 2));
            assertEquals(0b1000, regs.get(0));
        }

        @Test @DisplayName("or-int (0x96)") void orInt() {
            regs.put(1, 0b1010); regs.put(2, 0b0101);
            execute(0x96, arithInsn(0x96, 0, 1, 2));
            assertEquals(0b1111, regs.get(0));
        }

        @Test @DisplayName("xor-int (0x97)") void xorInt() {
            regs.put(1, 0b1100); regs.put(2, 0b1010);
            execute(0x97, arithInsn(0x97, 0, 1, 2));
            assertEquals(0b0110, regs.get(0));
        }

        @Test
        @DisplayName("BUG #7 — div-int (0x93) harus terdaftar")
        void divIntExists() {
            assertNotNull(instructionSet.getExecutor(0x93),
                "div-int (0x93) belum terdaftar — ini bug yang dilaporkan");
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // ARITHMETIC LIT8 — BUG #5 regression
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("ARITHMETIC lit8 (0xD8–0xE1) — BUG #5 regression")
    class ArithmeticLit8 {

        /**
         * Format 22b: units[0] = opcode | regA<<8 | regB<<12
         *             units[1] = literal (8-bit signed)
         *
         * BUG #5: implementasi saat ini membaca regB dari units[1] & 0xFF,
         * padahal regB ada di units[0] >> 12.
         * Test ini akan FAIL pada implementasi yang salah.
         */
        private DexInstruction lit8Insn(int opcode, int regA, int regB, int lit) {
            short[] units = {
                (short) ((opcode & 0xFF) | (regA << 8) | (regB << 12)),
                (short) (lit & 0xFF)
            };
            return insn(opcode, units, null, 0, -1);
        }

        @Test
        @DisplayName("add-int/lit8 (0xD8) — regB di units[0]>>12, bukan units[1]")
        void addLit8RegisterParsing() {
            regs.put(2, 10); // regB = 2
            // regA=0, regB=2, lit=5  → hasil = 10+5 = 15
            execute(0xD8, lit8Insn(0xD8, 0, 2, 5));
            assertEquals(15, regs.get(0),
                "BUG #5: regB harus dibaca dari units[0], bukan units[1]");
        }

        @Test
        @DisplayName("rsub-int/lit8 (0xD9) — semantik: literal - register")
        void rsubLit8() {
            regs.put(1, 3); // regB = 1, nilai = 3
            // literal=10, register=3 → 10-3 = 7
            execute(0xD9, lit8Insn(0xD9, 0, 1, 10));
            assertEquals(7, regs.get(0),
                "rsub = literal - register, bukan register - literal");
        }

        @Test
        @DisplayName("mul-int/lit8 (0xDA)")
        void mulLit8() {
            regs.put(1, 6);
            execute(0xDA, lit8Insn(0xDA, 0, 1, 7));
            assertEquals(42, regs.get(0));
        }

        @Test
        @DisplayName("and-int/lit8 (0xDD)")
        void andLit8() {
            regs.put(1, 0b1111);
            execute(0xDD, lit8Insn(0xDD, 0, 1, 0b1010));
            assertEquals(0b1010, regs.get(0));
        }

        @Test
        @DisplayName("shl-int/lit8 (0xE0)")
        void shlLit8() {
            regs.put(1, 1);
            execute(0xE0, lit8Insn(0xE0, 0, 1, 4));
            assertEquals(16, regs.get(0));
        }

        @Test
        @DisplayName("shr-int/lit8 (0xE1)")
        void shrLit8() {
            regs.put(1, 32);
            execute(0xE1, lit8Insn(0xE1, 0, 1, 2));
            assertEquals(8, regs.get(0));
        }

        @Test
        @DisplayName("literal negatif (sign-extend) bekerja benar")
        void lit8SignExtend() {
            regs.put(1, 0);
            // literal = 0xFF = -1 setelah sign-extend
            execute(0xD8, lit8Insn(0xD8, 0, 1, 0xFF));
            assertEquals(-1, regs.get(0));
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // ARITHMETIC 2addr — BUG #2 regression (register parsing dari opcode)
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("ARITHMETIC 2addr (0xB0–0xB7) — BUG #2 regression")
    class Arithmetic2addr {

        /**
         * Format 12x: units[0] = opcode | regA<<8 | regB<<12
         * BUG #2: executor membaca (opcode>>8)&0xF dan (opcode>>12)&0xF
         * tapi insn.getOpcode() hanya menyimpan byte pertama (0xB0...).
         * Register harus dari units[0].
         */
        private DexInstruction addr2Insn(int opcode, int regA, int regB) {
            short[] units = {(short) ((opcode & 0xFF) | (regA << 8) | (regB << 12))};
            return insn(opcode, units, null, 0, -1);
        }

        @Test
        @DisplayName("add-int/2addr (0xB0) — regA += regB")
        void addInt2addr() {
            regs.put(0, 10); regs.put(1, 5);
            execute(0xB0, addr2Insn(0xB0, 0, 1));
            assertEquals(15, regs.get(0));
        }

        @Test
        @DisplayName("sub-int/2addr (0xB1)")
        void subInt2addr() {
            regs.put(0, 10); regs.put(1, 3);
            execute(0xB1, addr2Insn(0xB1, 0, 1));
            assertEquals(7, regs.get(0));
        }

        @Test
        @DisplayName("xor-int/2addr (0xB7)")
        void xorInt2addr() {
            regs.put(0, 0b1100); regs.put(1, 0b1010);
            execute(0xB7, addr2Insn(0xB7, 0, 1));
            assertEquals(0b0110, regs.get(0));
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // ARRAY INSTRUCTIONS — BUG #2 regression (array-length register parsing)
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("ARRAY instructions — BUG #2 regression")
    class ArrayInstructions {

        @Test
        @DisplayName("array-length (0x21) — BUG #2: register dari units[0], bukan opcode")
        void arrayLength() {
            int[] arr = {10, 20, 30};
            regs.put(1, arr);
            // Format 12x: units[0] = 0x21 | regA<<8 | regB<<12
            // regA=0 (dest), regB=1 (source array)
            short[] units = {(short) (0x21 | (0 << 8) | (1 << 12))};
            DexInstruction i = insn(0x21, units, null, 0, -1);
            execute(0x21, i);
            assertEquals(3, regs.get(0),
                "BUG #2: array-length harus baca register dari units[0], bukan getOpcode()");
        }

        @Test
        @DisplayName("array-length null-safe (return 0 jika null)")
        void arrayLengthNull() {
            regs.put(1, null);
            short[] units = {(short) (0x21 | (0 << 8) | (1 << 12))};
            DexInstruction i = insn(0x21, units, null, 0, -1);
            execute(0x21, i);
            assertEquals(0, regs.get(0));
        }

        @Test
        @DisplayName("new-array (0x23) membuat array dengan ukuran dari register")
        void newArray() {
            when(indexer.getTypePool().getSize()).thenReturn(10);
            when(indexer.getTypePool().getTypeName(0)).thenReturn("[I");
            regs.put(1, 5); // size = 5
            // regA=0 (hasil), regB=1 (size), index=0 (type "[I")
            short[] units = {(short) (0x23 | (0 << 8) | (1 << 12))};
            DexInstruction i = insn(0x23, units, null, 0, 0);
            execute(0x23, i);
            Object result = regs.get(0);
            assertNotNull(result);
            assertTrue(result instanceof int[]);
            assertEquals(5, ((int[]) result).length);
        }

        @Test
        @DisplayName("aget (0x44) mengambil elemen dari array")
        void aget() {
            int[] arr = {10, 20, 30};
            regs.put(1, arr);
            regs.put(2, 1); // index
            // Format 23x: units[0]=opcode|regA<<8, units[1]=regB|regC<<8
            short[] units = {(short)(0x44 | (0<<8)), (short)(0x01 | (0x02<<8))};
            DexInstruction i = insn(0x44, units, null, 0, -1);
            execute(0x44, i);
            assertEquals(20, regs.get(0));
        }

        @Test
        @DisplayName("aput (0x4B) menaruh nilai ke array")
        void aput() {
            int[] arr = new int[3];
            regs.put(0, 99); // nilai
            regs.put(1, arr);
            regs.put(2, 2); // index
            short[] units = {(short)(0x4B | (0<<8)), (short)(0x01 | (0x02<<8))};
            DexInstruction i = insn(0x4B, units, null, 0, -1);
            execute(0x4B, i);
            assertEquals(99, arr[2]);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // FIELD ACCESS — BUG #6 regression (iget/iput format 22c bukan 23x)
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("FIELD ACCESS — BUG #6 regression")
    class FieldAccess {

        @Test
        @DisplayName("iget (0x52) membaca instance field")
        void iget() {
            Object instance = new Object();
            regs.put(1, instance);
            when(indexer.getFieldPool().getFieldSignature(0)).thenReturn("Lfoo/Bar;->name:Ljava/lang/String;");
            when(state.getInstanceField(instance, "Lfoo/Bar;->name:Ljava/lang/String;")).thenReturn("test_value");

            // Format 22c: units[0]=opcode|regA<<8|regB<<12, units[1]=field_index
            short[] units = {(short)(0x52 | (0<<8) | (1<<12)), (short)0};
            DexInstruction i = insn(0x52, units, null, 0, 0);
            execute(0x52, i);
            assertEquals("test_value", regs.get(0),
                "BUG #6: iget harus pakai format 22c, bukan getRegs23x()");
        }

        @Test
        @DisplayName("iput (0x59) menulis instance field")
        void iput() {
            Object instance = new Object();
            regs.put(0, "new_value");
            regs.put(1, instance);
            when(indexer.getFieldPool().getFieldSignature(0)).thenReturn("Lfoo/Bar;->name:Ljava/lang/String;");

            short[] units = {(short)(0x59 | (0<<8) | (1<<12)), (short)0};
            DexInstruction i = insn(0x59, units, null, 0, 0);
            execute(0x59, i);
            verify(state).setInstanceField(instance, "Lfoo/Bar;->name:Ljava/lang/String;", "new_value");
        }

        @Test
        @DisplayName("sget (0x60) membaca static field")
        void sget() {
            when(indexer.getFieldPool().getFieldSignature(2)).thenReturn("Lfoo/Bar;->count:I");
            when(state.getStaticField("Lfoo/Bar;->count:I")).thenReturn(42);
            // Format 21c: units[0] = opcode | regA<<8, units[1] = field_idx
            short[] units = {(short)(0x60 | (0<<8)), (short)2};
            DexInstruction i = insn(0x60, units, null, 0, 2);
            execute(0x60, i);
            assertEquals(42, regs.get(0));
        }

        @Test
        @DisplayName("sput (0x67) menulis static field")
        void sput() {
            regs.put(0, 99);
            when(indexer.getFieldPool().getFieldSignature(1)).thenReturn("Lfoo/Bar;->count:I");
            short[] units = {(short)(0x67 | (0<<8)), (short)1};
            DexInstruction i = insn(0x67, units, null, 0, 1);
            execute(0x67, i);
            verify(state).setStaticField("Lfoo/Bar;->count:I", 99);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // NEW-INSTANCE
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("NEW-INSTANCE (0x22)")
    class NewInstance {

        @Test
        @DisplayName("StringBuilder diinstansiasi")
        void newStringBuilder() {
            when(indexer.getTypePool().getSize()).thenReturn(10);
            when(indexer.getTypePool().getTypeName(0)).thenReturn("Ljava/lang/StringBuilder;");
            DexInstruction i = insn(0x22, null, new int[]{0}, 0, 0);
            execute(0x22, i);
            assertTrue(regs.get(0) instanceof StringBuilder);
        }

        @Test
        @DisplayName("tipe tidak dikenal mengembalikan Object")
        void newUnknownType() {
            when(indexer.getTypePool().getSize()).thenReturn(10);
            when(indexer.getTypePool().getTypeName(0)).thenReturn("Lsome/Unknown;");
            DexInstruction i = insn(0x22, null, new int[]{0}, 0, 0);
            execute(0x22, i);
            assertNotNull(regs.get(0));
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // CASTS
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("CASTS (0x81, 0x8D, 0x8E, 0x8F)")
    class Casts {

        private DexInstruction castInsn(int opcode, int regA, int regB) {
            short[] units = {(short)((opcode & 0xFF) | (regA<<8) | (regB<<12))};
            return insn(opcode, units, null, 0, -1);
        }

        @Test @DisplayName("int-to-byte (0x81) — masking 0xFF")
        void intToByte() {
            regs.put(1, 0x1FF); // 511 → 0xFF setelah masking = 255
            execute(0x81, castInsn(0x81, 0, 1));
            assertEquals(0xFF, regs.get(0));
        }

        @Test @DisplayName("int-to-byte (0x8D) — sign extend byte")
        void intToByteSignExtend() {
            regs.put(1, 0x80); // 128 → -128 sebagai signed byte
            execute(0x8D, castInsn(0x8D, 0, 1));
            assertEquals(-128, regs.get(0));
        }

        @Test @DisplayName("int-to-short (0x8E)")
        void intToShort() {
            regs.put(1, 0x8000); // 32768 → -32768 sebagai signed short
            execute(0x8E, castInsn(0x8E, 0, 1));
            assertEquals(-32768, regs.get(0));
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // GOTO & BRANCHING
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("GOTO & BRANCHING (0x28-0x2A, 0x32-0x3D)")
    class GotoAndBranching {

        @Test
        @DisplayName("goto (0x28) sets nextOffset")
        void gotoSetsNextOffset() {
            short[] units = {0x0528};
            DexInstruction i = insn(0x0528, units, null, 0, -1);
            when(i.getOffset()).thenReturn(10);
            execute(0x28, i);
            verify(state).setNextOffset(15);
        }

        @Test
        @DisplayName("if-eqz (0x38) branch taken sets nextOffset")
        void ifEqzBranchTaken() {
            regs.put(2, 0);
            short[] units = {0x0238, 5};
            DexInstruction i = insn(0x0238, units, null, 0, -1);
            when(i.getOffset()).thenReturn(10);
            execute(0x38, i);
            verify(state).setNextOffset(15);
        }

        @Test
        @DisplayName("if-eqz (0x38) branch not taken does not set nextOffset")
        void ifEqzBranchNotTaken() {
            regs.put(2, 42);
            short[] units = {0x0238, 5};
            DexInstruction i = insn(0x0238, units, null, 0, -1);
            when(i.getOffset()).thenReturn(10);
            execute(0x38, i);
            verify(state, never()).setNextOffset(anyInt());
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // COMPARISONS
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("COMPARISONS (0x2D-0x31)")
    class Comparisons {

        private DexInstruction cmpInsn(int opcode, int regA, int regB, int regC) {
            short[] units = {
                (short) ((opcode & 0xFF) | (regA << 8)),
                (short) ((regB & 0xFF) | (regC << 8))
            };
            return insn(opcode, units, null, 0, -1);
        }

        @Test
        @DisplayName("cmp-long (0x2D) compares two longs")
        void cmpLong() {
            regs.put(1, 100L);
            regs.put(2, 50L);
            execute(0x2D, cmpInsn(0x2D, 0, 1, 2));
            assertEquals(1, regs.get(0));

            regs.put(1, 50L);
            regs.put(2, 100L);
            execute(0x2D, cmpInsn(0x2D, 0, 1, 2));
            assertEquals(-1, regs.get(0));

            regs.put(1, 100L);
            regs.put(2, 100L);
            execute(0x2D, cmpInsn(0x2D, 0, 1, 2));
            assertEquals(0, regs.get(0));
        }

        @Test
        @DisplayName("cmpl-float (0x2E) handles NaN with -1")
        void cmplFloatNaN() {
            regs.put(1, Float.NaN);
            regs.put(2, 1.0f);
            execute(0x2E, cmpInsn(0x2E, 0, 1, 2));
            assertEquals(-1, regs.get(0));
        }

        @Test
        @DisplayName("cmpg-float (0x2F) handles NaN with 1")
        void cmpgFloatNaN() {
            regs.put(1, Float.NaN);
            regs.put(2, 1.0f);
            execute(0x2F, cmpInsn(0x2F, 0, 1, 2));
            assertEquals(1, regs.get(0));
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // INVOKE INSTRUCTIONS
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("INVOKE (0x6E-0x78)")
    class Invoke {

        @Test
        @DisplayName("invoke-virtual (0x6E) invokes method and records result")
        void invokeVirtual() {
            DexMethodPool mp = mock(DexMethodPool.class);
            when(indexer.getMethodPool()).thenReturn(mp);
            when(mp.getMethodSignature(5)).thenReturn("Lfoo/Bar;->test()V");

            java.util.List<Object> args = java.util.Arrays.asList(1, 2);
            when(invokeHandlerMock.getInvokeArgs(any())).thenReturn(args);
            when(invokeHandlerMock.handleInvoke("Lfoo/Bar;->test()V", args)).thenReturn("result");

            DexInstruction i = insn(0x6E, null, null, 0, 5);
            execute(0x6E, i);

            verify(state).recordResult("Lfoo/Bar;->test()V", i, "result");
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // EXECUTOR COVERAGE — semua opcode yang diklaim terdaftar
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Executor coverage — semua opcode harus terdaftar")
    class ExecutorCoverage {

        @ParameterizedTest(name = "opcode=0x{0}")
        @ValueSource(ints = {
            0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0D,
            0x0A, 0x0B, 0x0C,
            0x12, 0x13, 0x14, 0x15, 0x1A, 0x1C,
            0x21, 0x22, 0x23,
            0x28, 0x29, 0x2A,
            0x2D, 0x2E, 0x2F, 0x30, 0x31,
            0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x3A, 0x3B, 0x3C, 0x3D,
            0x44, 0x45, 0x46, 0x47, 0x48, 0x49, 0x4A,
            0x4B, 0x4C, 0x4D, 0x4E, 0x4F, 0x50, 0x51,
            0x52, 0x53, 0x54, 0x55, 0x56, 0x57, 0x58,
            0x59, 0x5A, 0x5B, 0x5C, 0x5D, 0x5E, 0x5F,
            0x60, 0x61, 0x62, 0x63, 0x64, 0x65, 0x66,
            0x67, 0x68, 0x69, 0x6A, 0x6B, 0x6C, 0x6D,
            0x6E, 0x6F, 0x70, 0x71, 0x72, 0x74, 0x75, 0x76, 0x77, 0x78,
            0x81, 0x8D, 0x8E, 0x8F,
            0x90, 0x91, 0x92, 0x94, 0x95, 0x96, 0x97,
            0xB0, 0xB1, 0xB2, 0xB5, 0xB6, 0xB7,
            0xD8, 0xD9, 0xDA, 0xDD, 0xDE, 0xDF, 0xE0, 0xE1
        })
        @DisplayName("executor terdaftar")
        void executorRegistered(int opcode) {
            assertNotNull(instructionSet.getExecutor(opcode),
                "Tidak ada executor untuk opcode 0x" + Integer.toHexString(opcode));
        }
    }
}
