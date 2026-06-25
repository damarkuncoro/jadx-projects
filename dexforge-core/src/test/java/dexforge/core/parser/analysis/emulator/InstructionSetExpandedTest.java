package dexforge.core.parser.analysis.emulator;

import dexforge.core.parser.dex.model.DexInstruction;
import dexforge.core.parser.dex.service.DexFastIndexer;
import dexforge.core.parser.dex.sections.DexStringPool;
import dexforge.core.parser.dex.sections.DexTypePool;
import dexforge.core.parser.dex.sections.DexFieldPool;
import dexforge.core.parser.dex.sections.DexMethodPool;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test suite untuk InstructionSet yang telah di-expand.
 *
 * Cakupan baru di luar test sebelumnya:
 *  - GOTO (0x28–0x2A): executor terdaftar, no-op
 *  - IF-* (0x32–0x3D): executor terdaftar, no-op (logic ada di ControlFlowHandler)
 *  - INVOKE (0x6E–0x72, 0x74–0x78): delegasi ke MethodInvokeHandler
 *  - CMP family (0x2D–0x31): cmpl/cmpg-float/double, cmp-long
 *  - LONG arithmetic (0x9B–0xA5, 0xBB–0xC5): add/sub/mul/div/and/or/xor/shl/shr
 *  - FLOAT arithmetic (0xA6–0xAA, 0xC6–0xCA): add/sub/mul/div/rem
 *  - DOUBLE arithmetic (0xAB–0xAF, 0xCB–0xCF)
 *  - LIT16 arithmetic (0xD0–0xD7)
 *  - CAST full (0x81–0x8F): semua unary type conversion
 *  - UNARY int (0x7B–0x80): neg/not
 *  - RETURN family (0x0E–0x11): lastResult di-set
 *  - MONITOR/THROW/SWITCH/CHECK-CAST (no-op, tapi terdaftar)
 *  - FILLED-NEW-ARRAY (0x24–0x25)
 *  - CONST-WIDE (0x16–0x19)
 *  - INSTANCE-OF (0x20)
 */
@DisplayName("InstructionSet — Expanded Coverage")
class InstructionSetExpandedTest {

    private EmulatorState state;
    private DexFastIndexer indexer;
    private MethodInvokeHandler invokeHandler;
    private InstructionSet instructionSet;
    private Map<Integer, Object> regs;

    @BeforeEach
    void setUp() {
        state        = mock(EmulatorState.class);
        indexer      = mock(DexFastIndexer.class);
        invokeHandler = mock(MethodInvokeHandler.class);

        var sp = mock(DexStringPool.class);
        var tp = mock(DexTypePool.class);
        var fp = mock(DexFieldPool.class);
        var mp = mock(DexMethodPool.class);

        when(indexer.getStringPool()).thenReturn(sp);
        when(indexer.getTypePool()).thenReturn(tp);
        when(indexer.getFieldPool()).thenReturn(fp);
        when(indexer.getMethodPool()).thenReturn(mp);
        when(sp.getSize()).thenReturn(100);
        when(tp.getSize()).thenReturn(100);
        when(state.getRegisters()).thenReturn(new HashMap<>());

        instructionSet = new InstructionSet(state, indexer);
        instructionSet.setInvokeHandler(invokeHandler);

        regs = new HashMap<>();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────────────────────────────────

    private DexInstruction insn(int opcode, short[] units, int[] registers, long literal, int index) {
        DexInstruction i = mock(DexInstruction.class);
        when(i.getOpcode()).thenReturn(opcode);
        when(i.getUnits()).thenReturn(units);
        when(i.getRegisters()).thenReturn(registers);
        when(i.getLiteral()).thenReturn(literal);
        when(i.getIndex()).thenReturn(index);
        return i;
    }

    /** Format 12x: units[0] = op | regA<<8 | regB<<12 */
    private DexInstruction insn12x(int op, int regA, int regB) {
        return insn(op, new short[]{(short)(op | (regA<<8) | (regB<<12))}, null, 0, -1);
    }

    /** Format 23x: units[0]=op|regA<<8, units[1]=regB|regC<<8 */
    private DexInstruction insn23x(int op, int regA, int regB, int regC) {
        return insn(op,
            new short[]{(short)(op|(regA<<8)), (short)((regB&0xFF)|((regC&0xFF)<<8))},
            null, 0, -1);
    }

    /** Format 22b (lit8): units[0]=op|regA<<8|regB<<12, units[1]=lit */
    private DexInstruction insnLit8(int op, int regA, int regB, int lit) {
        return insn(op,
            new short[]{(short)(op|(regA<<8)|(regB<<12)), (short)(lit&0xFF)},
            new int[]{regA, regB}, lit, -1);
    }

    /** Format 22s (lit16): units[0]=op|regA<<8|regB<<12, units[1]=lit16 */
    private DexInstruction insnLit16(int op, int regA, int regB, short lit) {
        return insn(op,
            new short[]{(short)(op|(regA<<8)|(regB<<12)), lit},
            null, lit, -1);
    }

    private void exec(int opcode, DexInstruction i) {
        InstructionExecutor ex = instructionSet.getExecutor(opcode);
        assertNotNull(ex, "Executor tidak ada untuk opcode 0x" + Integer.toHexString(opcode));
        ex.execute(i, regs);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // GOTO FAMILY — harus terdaftar sebagai no-op
    // ══════════════════════════════════════════════════════════════════════════

    @Nested @DisplayName("GOTO (0x28–0x2A) — no-op executor")
    class GotoFamily {
        @ParameterizedTest @ValueSource(ints = {0x28, 0x29, 0x2A})
        void gotoIsRegisteredAndNoOp(int op) {
            regs.put(0, "sentinel");
            exec(op, insn(op, new short[]{(short)op}, null, 0, -1));
            assertEquals("sentinel", regs.get(0), "GOTO tidak boleh ubah register");
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // IF-* FAMILY — harus terdaftar sebagai no-op
    // ══════════════════════════════════════════════════════════════════════════

    @Nested @DisplayName("IF-* (0x32–0x3D) — no-op executor")
    class IfFamily {
        @ParameterizedTest @ValueSource(ints = {0x32,0x33,0x34,0x35,0x36,0x37,0x38,0x39,0x3A,0x3B,0x3C,0x3D})
        void ifIsRegisteredAndNoOp(int op) {
            regs.put(0, 99);
            exec(op, insn(op, new short[]{(short)(op|(0<<8)|(1<<12)),(short)2}, null, 0, -1));
            assertEquals(99, regs.get(0));
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // RETURN FAMILY — harus set lastResult
    // ══════════════════════════════════════════════════════════════════════════

    @Nested @DisplayName("RETURN family (0x0E–0x11)")
    class ReturnFamily {

        @Test @DisplayName("return-void (0x0E) set lastResult null")
        void returnVoid() {
            exec(0x0E, insn(0x0E, new short[]{(short)0x0E}, null, 0, -1));
            verify(state).setLastResult(null);
        }

        @Test @DisplayName("return (0x0F) set lastResult ke nilai register")
        void returnValue() {
            regs.put(3, 42);
            exec(0x0F, insn(0x0F, new short[]{(short)(0x0F|(3<<8))}, null, 0, -1));
            verify(state).setLastResult(42);
        }

        @Test @DisplayName("return-object (0x11) set lastResult ke objek")
        void returnObject() {
            Object obj = new Object();
            regs.put(1, obj);
            exec(0x11, insn(0x11, new short[]{(short)(0x11|(1<<8))}, null, 0, -1));
            verify(state).setLastResult(obj);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // MONITOR / THROW / SWITCH / CHECK-CAST — terdaftar dan no-op / minimal effect
    // ══════════════════════════════════════════════════════════════════════════

    @Nested @DisplayName("Monitor / Throw / Switch / Check-cast")
    class MonitorThrowSwitch {

        @ParameterizedTest @ValueSource(ints = {0x1D, 0x1E, 0x1F, 0x2B, 0x2C})
        void noOpOpcodes(int op) {
            assertNotNull(instructionSet.getExecutor(op));
            assertDoesNotThrow(() -> exec(op, insn(op, new short[]{(short)op}, null, 0, -1)));
        }

        @Test @DisplayName("throw (0x27) menyimpan exception ke lastResult")
        void throwSavesException() {
            Object ex = new RuntimeException("test");
            regs.put(2, ex);
            exec(0x27, insn(0x27, new short[]{(short)(0x27|(2<<8))}, null, 0, -1));
            verify(state).setLastResult(ex);
        }

        @Test @DisplayName("instance-of (0x20) selalu set regA = 1")
        void instanceOfAlwaysTrue() {
            exec(0x20, insn12x(0x20, 0, 1));
            assertEquals(1, regs.get(0));
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // CMP FAMILY
    // ══════════════════════════════════════════════════════════════════════════

    @Nested @DisplayName("CMP family (0x2D–0x31)")
    class CmpFamily {

        @Test @DisplayName("cmp-long (0x31): a < b → -1")
        void cmpLongLess() {
            regs.put(1, 5L); regs.put(2, 10L);
            exec(0x31, insn23x(0x31, 0, 1, 2));
            assertTrue((int)regs.get(0) < 0);
        }

        @Test @DisplayName("cmp-long (0x31): a == b → 0")
        void cmpLongEqual() {
            regs.put(1, 7L); regs.put(2, 7L);
            exec(0x31, insn23x(0x31, 0, 1, 2));
            assertEquals(0, regs.get(0));
        }

        @Test @DisplayName("cmp-long (0x31): a > b → +1")
        void cmpLongGreater() {
            regs.put(1, 10L); regs.put(2, 3L);
            exec(0x31, insn23x(0x31, 0, 1, 2));
            assertTrue((int)regs.get(0) > 0);
        }

        @Test @DisplayName("cmpl-float (0x2D): NaN → -1")
        void cmplFloatNaN() {
            regs.put(1, Float.NaN); regs.put(2, 1.0f);
            exec(0x2D, insn23x(0x2D, 0, 1, 2));
            assertEquals(-1, regs.get(0));
        }

        @Test @DisplayName("cmpg-float (0x2E): NaN → +1")
        void cmpgFloatNaN() {
            regs.put(1, Float.NaN); regs.put(2, 1.0f);
            exec(0x2E, insn23x(0x2E, 0, 1, 2));
            assertEquals(1, regs.get(0));
        }

        @Test @DisplayName("cmpl-float (0x2D): 3.0 > 2.0 → +1")
        void cmplFloatGreater() {
            regs.put(1, 3.0f); regs.put(2, 2.0f);
            exec(0x2D, insn23x(0x2D, 0, 1, 2));
            assertTrue((int)regs.get(0) > 0);
        }

        @Test @DisplayName("cmpl-double (0x2F): NaN → -1")
        void cmplDoubleNaN() {
            regs.put(1, Double.NaN); regs.put(2, 1.0d);
            exec(0x2F, insn23x(0x2F, 0, 1, 2));
            assertEquals(-1, regs.get(0));
        }

        @Test @DisplayName("cmpg-double (0x30): NaN → +1")
        void cmpgDoubleNaN() {
            regs.put(1, Double.NaN); regs.put(2, 1.0d);
            exec(0x30, insn23x(0x30, 0, 1, 2));
            assertEquals(1, regs.get(0));
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // INVOKE FAMILY
    // ══════════════════════════════════════════════════════════════════════════

    @Nested @DisplayName("INVOKE family (0x6E–0x72, 0x74–0x78)")
    class InvokeFamily {

        @ParameterizedTest @ValueSource(ints = {0x6E, 0x6F, 0x70, 0x71, 0x72, 0x74, 0x75, 0x76, 0x77, 0x78})
        @DisplayName("semua invoke opcode terdaftar")
        void invokeOpcodeRegistered(int op) {
            assertNotNull(instructionSet.getExecutor(op));
        }

        @Test @DisplayName("invoke-static (0x71) memanggil MethodInvokeHandler")
        void invokeStaticCallsHandler() {
            when(indexer.getMethodPool().getMethodSignature(5))
                .thenReturn("Ljava/lang/String;->valueOf(I)Ljava/lang/String;");
            when(invokeHandler.getInvokeArgs(any())).thenReturn(java.util.List.of(42));
            when(invokeHandler.handleInvoke(any(), any())).thenReturn("42");

            // Format 35c: units[0]=op|(count<<12), units[1]=method_idx, units[2]=regs
            DexInstruction i = insn(0x71,
                new short[]{(short)(0x71|(1<<12)), 5, (short)(1<<0)},
                null, 0, 5);
            exec(0x71, i);

            verify(invokeHandler).handleInvoke(
                eq("Ljava/lang/String;->valueOf(I)Ljava/lang/String;"), any());
        }

        @Test @DisplayName("invoke-static (0x71) memanggil MethodInvokeHandler range format")
        void invokeStaticCallsHandlerRange() {
            when(indexer.getMethodPool().getMethodSignature(5))
                .thenReturn("Ljava/lang/String;->valueOf(I)Ljava/lang/String;");
            when(invokeHandler.getInvokeArgs(any())).thenReturn(java.util.List.of(42));
            when(invokeHandler.handleInvoke(any(), any())).thenReturn("42");

            // Format 3rc: units[0]=op|(count<<8), units[1]=method_idx, units[2]=start_reg
            DexInstruction i = insn(0x77,
                new short[]{(short)(0x77|(1<<8)), 5, 2},
                null, 0, 5);
            exec(0x77, i);

            verify(invokeHandler).handleInvoke(
                eq("Ljava/lang/String;->valueOf(I)Ljava/lang/String;"), any());
        }

        @Test @DisplayName("invoke tanpa invokeHandler — tidak throw")
        void invokeWithoutHandlerSafe() {
            InstructionSet is2 = new InstructionSet(state, indexer);
            // invokeHandler tidak di-set
            DexInstruction i = insn(0x71, new short[]{0x71, 0, 0}, null, 0, 0);
            assertDoesNotThrow(() -> is2.getExecutor(0x71).execute(i, regs));
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // LONG ARITHMETIC
    // ══════════════════════════════════════════════════════════════════════════

    @Nested @DisplayName("LONG arithmetic (0x9B–0xA5, 0xBB–0xC5)")
    class LongArithmetic {

        @Test @DisplayName("add-long (0x9B)")
        void addLong() {
            regs.put(1, 1_000_000_000L); regs.put(2, 2_000_000_000L);
            exec(0x9B, insn23x(0x9B, 0, 1, 2));
            assertEquals(3_000_000_000L, regs.get(0));
        }

        @Test @DisplayName("sub-long (0x9C)")
        void subLong() {
            regs.put(1, 5_000_000_000L); regs.put(2, 3_000_000_000L);
            exec(0x9C, insn23x(0x9C, 0, 1, 2));
            assertEquals(2_000_000_000L, regs.get(0));
        }

        @Test @DisplayName("mul-long (0x9D)")
        void mulLong() {
            regs.put(1, 100_000L); regs.put(2, 100_000L);
            exec(0x9D, insn23x(0x9D, 0, 1, 2));
            assertEquals(10_000_000_000L, regs.get(0));
        }

        @Test @DisplayName("div-long (0x9E) — div by zero → 0")
        void divLongByZero() {
            regs.put(1, 100L); regs.put(2, 0L);
            exec(0x9E, insn23x(0x9E, 0, 1, 2));
            assertEquals(0L, regs.get(0));
        }

        @Test @DisplayName("and-long (0xA0)")
        void andLong() {
            regs.put(1, 0xFF00FF00L); regs.put(2, 0xF0F0F0F0L);
            exec(0xA0, insn23x(0xA0, 0, 1, 2));
            assertEquals(0xFF00FF00L & 0xF0F0F0F0L, regs.get(0));
        }

        @Test @DisplayName("or-long (0xA1)")
        void orLong() {
            regs.put(1, 0xFF00L); regs.put(2, 0x00FFL);
            exec(0xA1, insn23x(0xA1, 0, 1, 2));
            assertEquals(0xFFFFL, regs.get(0));
        }

        @Test @DisplayName("xor-long (0xA2)")
        void xorLong() {
            regs.put(1, 0b1100L); regs.put(2, 0b1010L);
            exec(0xA2, insn23x(0xA2, 0, 1, 2));
            assertEquals(0b0110L, regs.get(0));
        }

        @Test @DisplayName("shl-long (0xA3) — shift amount dari int reg")
        void shlLong() {
            regs.put(1, 1L); regs.put(2, 40); // int shift amount
            exec(0xA3, insn23x(0xA3, 0, 1, 2));
            assertEquals(1L << 40, regs.get(0));
        }

        @Test @DisplayName("shr-long (0xA4)")
        void shrLong() {
            regs.put(1, 1L << 40); regs.put(2, 40);
            exec(0xA4, insn23x(0xA4, 0, 1, 2));
            assertEquals(1L, regs.get(0));
        }

        @Test @DisplayName("add-long/2addr (0xBB)")
        void addLong2addr() {
            regs.put(0, 1_000_000_000L); regs.put(1, 9_000_000_000L);
            exec(0xBB, insn12x(0xBB, 0, 1));
            assertEquals(10_000_000_000L, regs.get(0));
        }

        @Test @DisplayName("and-long/2addr (0xC0)")
        void andLong2addr() {
            regs.put(0, 0xFFL); regs.put(1, 0x0FL);
            exec(0xC0, insn12x(0xC0, 0, 1));
            assertEquals(0x0FL, regs.get(0));
        }

        @Test @DisplayName("shl-long/2addr (0xC3)")
        void shlLong2addr() {
            regs.put(0, 1L); regs.put(1, 32);
            exec(0xC3, insn12x(0xC3, 0, 1));
            assertEquals(1L << 32, regs.get(0));
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // FLOAT ARITHMETIC
    // ══════════════════════════════════════════════════════════════════════════

    @Nested @DisplayName("FLOAT arithmetic (0xA6–0xAA, 0xC6–0xCA)")
    class FloatArithmetic {

        @Test @DisplayName("add-float (0xA6)")
        void addFloat() {
            regs.put(1, 1.5f); regs.put(2, 2.5f);
            exec(0xA6, insn23x(0xA6, 0, 1, 2));
            assertEquals(4.0f, (float)regs.get(0), 1e-6f);
        }

        @Test @DisplayName("sub-float (0xA7)")
        void subFloat() {
            regs.put(1, 5.0f); regs.put(2, 3.0f);
            exec(0xA7, insn23x(0xA7, 0, 1, 2));
            assertEquals(2.0f, (float)regs.get(0), 1e-6f);
        }

        @Test @DisplayName("mul-float (0xA8)")
        void mulFloat() {
            regs.put(1, 3.0f); regs.put(2, 4.0f);
            exec(0xA8, insn23x(0xA8, 0, 1, 2));
            assertEquals(12.0f, (float)regs.get(0), 1e-6f);
        }

        @Test @DisplayName("div-float (0xA9) — div by zero → NaN")
        void divFloatByZero() {
            regs.put(1, 1.0f); regs.put(2, 0.0f);
            exec(0xA9, insn23x(0xA9, 0, 1, 2));
            assertTrue(Float.isNaN((float)regs.get(0)));
        }

        @Test @DisplayName("rem-float (0xAA)")
        void remFloat() {
            regs.put(1, 10.0f); regs.put(2, 3.0f);
            exec(0xAA, insn23x(0xAA, 0, 1, 2));
            assertEquals(1.0f, (float)regs.get(0), 1e-6f);
        }

        @Test @DisplayName("add-float/2addr (0xC6)")
        void addFloat2addr() {
            regs.put(0, 1.0f); regs.put(1, 2.0f);
            exec(0xC6, insn12x(0xC6, 0, 1));
            assertEquals(3.0f, (float)regs.get(0), 1e-6f);
        }

        @Test @DisplayName("mul-float/2addr (0xC8)")
        void mulFloat2addr() {
            regs.put(0, 2.5f); regs.put(1, 4.0f);
            exec(0xC8, insn12x(0xC8, 0, 1));
            assertEquals(10.0f, (float)regs.get(0), 1e-6f);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // DOUBLE ARITHMETIC
    // ══════════════════════════════════════════════════════════════════════════

    @Nested @DisplayName("DOUBLE arithmetic (0xAB–0xAF, 0xCB–0xCF)")
    class DoubleArithmetic {

        @Test @DisplayName("add-double (0xAB)")
        void addDouble() {
            regs.put(1, 1.5d); regs.put(2, 2.5d);
            exec(0xAB, insn23x(0xAB, 0, 1, 2));
            assertEquals(4.0d, (double)regs.get(0), 1e-9d);
        }

        @Test @DisplayName("mul-double (0xAD)")
        void mulDouble() {
            regs.put(1, 1.23456789d); regs.put(2, 2.0d);
            exec(0xAD, insn23x(0xAD, 0, 1, 2));
            assertEquals(2.46913578d, (double)regs.get(0), 1e-9d);
        }

        @Test @DisplayName("div-double (0xAE) — div by zero → NaN")
        void divDoubleByZero() {
            regs.put(1, 1.0d); regs.put(2, 0.0d);
            exec(0xAE, insn23x(0xAE, 0, 1, 2));
            assertTrue(Double.isNaN((double)regs.get(0)));
        }

        @Test @DisplayName("add-double/2addr (0xCB)")
        void addDouble2addr() {
            regs.put(0, 10.0d); regs.put(1, 5.5d);
            exec(0xCB, insn12x(0xCB, 0, 1));
            assertEquals(15.5d, (double)regs.get(0), 1e-9d);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // LIT16 ARITHMETIC
    // ══════════════════════════════════════════════════════════════════════════

    @Nested @DisplayName("LIT16 arithmetic (0xD0–0xD7)")
    class Lit16Arithmetic {

        @Test @DisplayName("add-int/lit16 (0xD0)")
        void addLit16() {
            regs.put(1, 100);
            exec(0xD0, insnLit16(0xD0, 0, 1, (short)200));
            assertEquals(300, regs.get(0));
        }

        @Test @DisplayName("rsub-int/lit16 (0xD1): lit - reg")
        void rsubLit16() {
            regs.put(1, 3);
            exec(0xD1, insnLit16(0xD1, 0, 1, (short)10));
            assertEquals(7, regs.get(0));
        }

        @Test @DisplayName("mul-int/lit16 (0xD2)")
        void mulLit16() {
            regs.put(1, 7);
            exec(0xD2, insnLit16(0xD2, 0, 1, (short)6));
            assertEquals(42, regs.get(0));
        }

        @Test @DisplayName("and-int/lit16 (0xD5)")
        void andLit16() {
            regs.put(1, 0b1111);
            exec(0xD5, insnLit16(0xD5, 0, 1, (short)0b1010));
            assertEquals(0b1010, regs.get(0));
        }

        @Test @DisplayName("div-int/lit16 (0xD3) — div by zero → 0")
        void divLit16ByZero() {
            regs.put(1, 100);
            exec(0xD3, insnLit16(0xD3, 0, 1, (short)0));
            assertEquals(0, regs.get(0));
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // UNARY INT (neg, not)
    // ══════════════════════════════════════════════════════════════════════════

    @Nested @DisplayName("UNARY int/long (0x7B–0x80)")
    class UnaryOps {

        @Test @DisplayName("neg-int (0x7B)")
        void negInt() {
            regs.put(1, 42);
            exec(0x7B, insn12x(0x7B, 0, 1));
            assertEquals(-42, regs.get(0));
        }

        @Test @DisplayName("not-int (0x7C)")
        void notInt() {
            regs.put(1, 0);
            exec(0x7C, insn12x(0x7C, 0, 1));
            assertEquals(-1, regs.get(0));
        }

        @Test @DisplayName("neg-long (0x7D)")
        void negLong() {
            regs.put(1, 5_000_000_000L);
            exec(0x7D, insn12x(0x7D, 0, 1));
            assertEquals(-5_000_000_000L, regs.get(0));
        }

        @Test @DisplayName("not-long (0x7E)")
        void notLong() {
            regs.put(1, 0L);
            exec(0x7E, insn12x(0x7E, 0, 1));
            assertEquals(-1L, regs.get(0));
        }

        @Test @DisplayName("neg-float (0x7F)")
        void negFloat() {
            regs.put(1, 3.14f);
            exec(0x7F, insn12x(0x7F, 0, 1));
            assertEquals(-3.14f, (float)regs.get(0), 1e-6f);
        }

        @Test @DisplayName("neg-double (0x80)")
        void negDouble() {
            regs.put(1, 2.718281828d);
            exec(0x80, insn12x(0x80, 0, 1));
            assertEquals(-2.718281828d, (double)regs.get(0), 1e-9d);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // FULL CAST COVERAGE
    // ══════════════════════════════════════════════════════════════════════════

    @Nested @DisplayName("CAST full suite (0x81–0x8F)")
    class CastFull {

        @Test @DisplayName("int-to-long (0x81)")
        void intToLong() {
            regs.put(1, 42);
            exec(0x81, insn12x(0x81, 0, 1));
            assertEquals(42L, regs.get(0));
        }

        @Test @DisplayName("int-to-float (0x82)")
        void intToFloat() {
            regs.put(1, 10);
            exec(0x82, insn12x(0x82, 0, 1));
            assertEquals(10.0f, regs.get(0));
        }

        @Test @DisplayName("int-to-double (0x83)")
        void intToDouble() {
            regs.put(1, 10);
            exec(0x83, insn12x(0x83, 0, 1));
            assertEquals(10.0d, regs.get(0));
        }

        @Test @DisplayName("long-to-int (0x84) — truncates")
        void longToInt() {
            regs.put(1, 0x1_FFFF_FFFFL);
            exec(0x84, insn12x(0x84, 0, 1));
            assertEquals(-1, regs.get(0)); // 0xFFFFFFFF as signed int
        }

        @Test @DisplayName("long-to-float (0x85)")
        void longToFloat() {
            regs.put(1, 100L);
            exec(0x85, insn12x(0x85, 0, 1));
            assertEquals(100.0f, (float)regs.get(0), 1e-4f);
        }

        @Test @DisplayName("float-to-int (0x87) — truncates toward zero")
        void floatToInt() {
            regs.put(1, 3.99f);
            exec(0x87, insn12x(0x87, 0, 1));
            assertEquals(3, regs.get(0));
        }

        @Test @DisplayName("float-to-long (0x88)")
        void floatToLong() {
            regs.put(1, 1e10f);
            exec(0x88, insn12x(0x88, 0, 1));
            assertEquals((long)1e10f, regs.get(0));
        }

        @Test @DisplayName("double-to-int (0x8A)")
        void doubleToInt() {
            regs.put(1, 3.99d);
            exec(0x8A, insn12x(0x8A, 0, 1));
            assertEquals(3, regs.get(0));
        }

        @Test @DisplayName("double-to-float (0x8C)")
        void doubleToFloat() {
            regs.put(1, 3.14d);
            exec(0x8C, insn12x(0x8C, 0, 1));
            assertEquals(3.14f, (float)regs.get(0), 1e-5f);
        }

        @Test @DisplayName("int-to-byte (0x8D) — sign extend")
        void intToByte() {
            regs.put(1, 0x80); // 128 → -128
            exec(0x8D, insn12x(0x8D, 0, 1));
            assertEquals(-128, regs.get(0));
        }

        @Test @DisplayName("int-to-char (0x8E) — unsigned 16-bit")
        void intToChar() {
            regs.put(1, 0x10041); // U+0041 mod 0xFFFF
            exec(0x8E, insn12x(0x8E, 0, 1));
            assertEquals(0x41, regs.get(0)); // 'A'
        }

        @Test @DisplayName("int-to-short (0x8F) — sign extend")
        void intToShort() {
            regs.put(1, 0x8000); // 32768 → -32768
            exec(0x8F, insn12x(0x8F, 0, 1));
            assertEquals(-32768, regs.get(0));
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // CONST-WIDE
    // ══════════════════════════════════════════════════════════════════════════

    @Nested @DisplayName("CONST-WIDE (0x16–0x19)")
    class ConstWide {

        @ParameterizedTest @ValueSource(ints = {0x16, 0x17, 0x18, 0x19})
        @DisplayName("const-wide menaruh long ke register pair")
        void constWideStoresLong(int op) {
            long lit = 0x1234_5678_9ABC_DEF0L;
            DexInstruction i = insn(op, new short[]{(short)op}, new int[]{0}, lit, -1);
            exec(op, i);
            assertEquals(lit, regs.get(0));
            // high 32-bit di register 1
            assertEquals((int)(lit >> 32), regs.get(1));
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // FILLED-NEW-ARRAY
    // ══════════════════════════════════════════════════════════════════════════

    @Nested @DisplayName("FILLED-NEW-ARRAY (0x24–0x25)")
    class FilledNewArray {

        @Test @DisplayName("filled-new-array (0x24) membuat array dari register")
        void filledNewArray() {
            regs.put(0, 10); regs.put(1, 20); regs.put(2, 30);
            // regCount=3, g=0 (last reg slot), c=0, d=1, e=2, f=0
            // Format 35c: units[0] = op|(regCount<<12)|(g<<8), units[1]=type_idx, units[2]=c|(d<<4)|(e<<8)|(f<<12)
            DexInstruction i = insn(0x24,
                new short[]{(short)(0x24|(3<<12)|(0<<8)), 0, (short)(0|(1<<4)|(2<<8))},
                null, 0, 0);
            exec(0x24, i);
            verify(state).setLastResult(argThat(r -> {
                if (!(r instanceof Object[])) return false;
                Object[] arr = (Object[]) r;
                return arr.length == 3 && arr[0].equals(10) && arr[1].equals(20) && arr[2].equals(30);
            }));
        }

        @Test @DisplayName("filled-new-array/range (0x25) membuat array dari range")
        void filledNewArrayRange() {
            regs.put(2, 100); regs.put(3, 200); regs.put(4, 300);
            // regCount=3, firstReg=2
            DexInstruction i = insn(0x25,
                new short[]{(short)(0x25|(3<<8)), 0, (short)2},
                null, 0, 0);
            exec(0x25, i);
            verify(state).setLastResult(argThat(r -> {
                if (!(r instanceof Object[])) return false;
                Object[] arr = (Object[]) r;
                return arr.length == 3 && arr[0].equals(100) && arr[1].equals(200) && arr[2].equals(300);
            }));
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // INT ARITHMETIC COMPLETENESS — div/rem dengan div-by-zero
    // ══════════════════════════════════════════════════════════════════════════

    @Nested @DisplayName("INT arithmetic — div/rem safety")
    class IntArithmeticSafety {

        @Test @DisplayName("div-int (0x93) div by zero → 0")
        void divIntByZero() {
            regs.put(1, 100); regs.put(2, 0);
            exec(0x93, insn23x(0x93, 0, 1, 2));
            assertEquals(0, regs.get(0));
        }

        @Test @DisplayName("rem-int (0x94) div by zero → 0")
        void remIntByZero() {
            regs.put(1, 100); regs.put(2, 0);
            exec(0x94, insn23x(0x94, 0, 1, 2));
            assertEquals(0, regs.get(0));
        }

        @Test @DisplayName("div-int/lit8 (0xDB) div by zero → 0")
        void divLit8ByZero() {
            regs.put(1, 50);
            exec(0xDB, insnLit8(0xDB, 0, 1, 0));
            assertEquals(0, regs.get(0));
        }

        @Test @DisplayName("shl-int (0x98) — shift left 23x")
        void shlInt() {
            regs.put(1, 1); regs.put(2, 8);
            exec(0x98, insn23x(0x98, 0, 1, 2));
            assertEquals(256, regs.get(0));
        }

        @Test @DisplayName("ushr-int (0x9A) — unsigned shift right")
        void ushrInt() {
            regs.put(1, 0xFF000000); regs.put(2, 4);
            exec(0x9A, insn23x(0x9A, 0, 1, 2));
            assertEquals(0xFF000000 >>> 4, regs.get(0));
        }

        @Test @DisplayName("ushr-int/lit8 (0xE2)")
        void ushrLit8() {
            regs.put(1, 0xFF000000);
            exec(0xE2, insnLit8(0xE2, 0, 1, 4));
            assertEquals(0xFF000000 >>> 4, regs.get(0));
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // EXECUTOR COVERAGE — semua opcode yang diklaim terdaftar
    // ══════════════════════════════════════════════════════════════════════════

    @Nested @DisplayName("Executor coverage — semua opcode harus terdaftar")
    class ExecutorCoverage {

        @ParameterizedTest(name = "0x{0}")
        @ValueSource(ints = {
            // move
            0x01,0x02,0x03,0x04,0x05,0x06,0x07,0x08,0x09,0x0A,0x0B,0x0C,0x0D,
            // return
            0x0E,0x0F,0x10,0x11,
            // const
            0x12,0x13,0x14,0x15,0x16,0x17,0x18,0x19,0x1A,0x1B,0x1C,
            // misc
            0x1D,0x1E,0x1F,0x20,0x21,0x22,0x23,0x24,0x25,0x26,0x27,
            // goto
            0x28,0x29,0x2A,
            // switch
            0x2B,0x2C,
            // cmp
            0x2D,0x2E,0x2F,0x30,0x31,
            // if
            0x32,0x33,0x34,0x35,0x36,0x37,0x38,0x39,0x3A,0x3B,0x3C,0x3D,
            // array
            0x44,0x45,0x46,0x47,0x48,0x49,0x4A,
            0x4B,0x4C,0x4D,0x4E,0x4F,0x50,0x51,
            // field
            0x52,0x53,0x54,0x55,0x56,0x57,0x58,
            0x59,0x5A,0x5B,0x5C,0x5D,0x5E,0x5F,
            0x60,0x61,0x62,0x63,0x64,0x65,0x66,
            0x67,0x68,0x69,0x6A,0x6B,0x6C,0x6D,
            // invoke
            0x6E,0x6F,0x70,0x71,0x72,0x74,0x75,0x76,0x77,0x78,
            // unary
            0x7B,0x7C,0x7D,0x7E,0x7F,0x80,
            // cast
            0x81,0x82,0x83,0x84,0x85,0x86,0x87,0x88,0x89,0x8A,0x8B,0x8C,0x8D,0x8E,0x8F,
            // int arith 23x
            0x90,0x91,0x92,0x93,0x94,0x95,0x96,0x97,0x98,0x99,0x9A,
            // long arith 23x
            0x9B,0x9C,0x9D,0x9E,0x9F,0xA0,0xA1,0xA2,0xA3,0xA4,0xA5,
            // float arith 23x
            0xA6,0xA7,0xA8,0xA9,0xAA,
            // double arith 23x
            0xAB,0xAC,0xAD,0xAE,0xAF,
            // int arith 2addr
            0xB0,0xB1,0xB2,0xB3,0xB4,0xB5,0xB6,0xB7,0xB8,0xB9,0xBA,
            // long arith 2addr
            0xBB,0xBC,0xBD,0xBE,0xBF,0xC0,0xC1,0xC2,0xC3,0xC4,0xC5,
            // float arith 2addr
            0xC6,0xC7,0xC8,0xC9,0xCA,
            // double arith 2addr
            0xCB,0xCC,0xCD,0xCE,0xCF,
            // lit16
            0xD0,0xD1,0xD2,0xD3,0xD4,0xD5,0xD6,0xD7,
            // lit8
            0xD8,0xD9,0xDA,0xDB,0xDC,0xDD,0xDE,0xDF,0xE0,0xE1,0xE2
        })
        void executorRegistered(int op) {
            assertNotNull(instructionSet.getExecutor(op),
                "Tidak ada executor untuk opcode 0x" + Integer.toHexString(op));
        }
    }
}
