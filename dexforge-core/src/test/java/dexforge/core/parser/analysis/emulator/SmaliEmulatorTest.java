package dexforge.core.parser.analysis.emulator;

import static org.assertj.core.api.Assertions.assertThat;

import dexforge.core.parser.dex.model.DexInstruction;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class SmaliEmulatorTest {

    @Test
    void testSequentialExecution() {
        SmaliEmulator emulator = new SmaliEmulator();
        List<DexInstruction> instructions = new ArrayList<>();

        // const/4 v0, 5
        instructions.add(new DexInstruction(0x5012, -1, 0, 1, new short[]{(short) 0x5012}));
        // const/4 v1, 2
        instructions.add(new DexInstruction(0x2112, -1, 1, 1, new short[]{(short) 0x2112}));
        // add-int/2addr v0, v1
        instructions.add(new DexInstruction(0x1090, -1, 2, 1, new short[]{(short) 0x1090}));
        // return v0
        instructions.add(new DexInstruction(0x000F, -1, 3, 1, new short[]{(short) 0x000F}));

        Object result = emulator.execute(instructions, new HashMap<>());
        assertThat(result).isEqualTo(7);
    }

    @Test
    void testGotoJump() {
        SmaliEmulator emulator = new SmaliEmulator();
        List<DexInstruction> instructions = new ArrayList<>();

        // const/4 v0, 5
        instructions.add(new DexInstruction(0x5012, -1, 0, 1, new short[]{(short) 0x5012}));
        // goto +2 (skip next instruction, so target is offset 3)
        // rawOpcode: (2 << 8) | 0x28 = 0x0228
        instructions.add(new DexInstruction(0x0228, -1, 1, 1, new short[]{(short) 0x0228}));
        // const/4 v0, 7 (offset 2) - should be skipped
        instructions.add(new DexInstruction(0x7012, -1, 2, 1, new short[]{(short) 0x7012}));
        // return v0 (offset 3)
        instructions.add(new DexInstruction(0x000F, -1, 3, 1, new short[]{(short) 0x000F}));

        Object result = emulator.execute(instructions, new HashMap<>());
        assertThat(result).isEqualTo(5);
    }

    @Test
    void testIfEqzBranch() {
        SmaliEmulator emulator = new SmaliEmulator();
        List<DexInstruction> instructions = new ArrayList<>();

        // const/4 v0, 0
        instructions.add(new DexInstruction(0x0012, -1, 0, 1, new short[]{(short) 0x0012}));
        // if-eqz v0, +3 (target is offset 4)
        // units: [ 0x0038, 3 ]
        instructions.add(new DexInstruction(0x0038, -1, 1, 2, new short[]{(short) 0x0038, 3}));
        // const/4 v0, 7 (offset 3) - should be skipped
        instructions.add(new DexInstruction(0x7012, -1, 3, 1, new short[]{(short) 0x7012}));
        // return v0 (offset 4)
        instructions.add(new DexInstruction(0x000F, -1, 4, 1, new short[]{(short) 0x000F}));

        Object result = emulator.execute(instructions, new HashMap<>());
        assertThat(result).isEqualTo(0);
    }

    @Test
    void testIfNezBranch() {
        SmaliEmulator emulator = new SmaliEmulator();
        List<DexInstruction> instructions = new ArrayList<>();

        // const/4 v0, 5
        instructions.add(new DexInstruction(0x5012, -1, 0, 1, new short[]{(short) 0x5012}));
        // if-nez v0, +3 (target is offset 4)
        // units: [ 0x0039, 3 ]
        instructions.add(new DexInstruction(0x0039, -1, 1, 2, new short[]{(short) 0x0039, 3}));
        // const/4 v0, 7 (offset 3) - should be skipped
        instructions.add(new DexInstruction(0x7012, -1, 3, 1, new short[]{(short) 0x7012}));
        // return v0 (offset 4)
        instructions.add(new DexInstruction(0x000F, -1, 4, 1, new short[]{(short) 0x000F}));

        Object result = emulator.execute(instructions, new HashMap<>());
        assertThat(result).isEqualTo(5);
    }

    @Test
    void testLoopExecution() {
        SmaliEmulator emulator = new SmaliEmulator();
        List<DexInstruction> instructions = new ArrayList<>();

        // const/4 v0, 0 (offset 0) - accumulator
        instructions.add(new DexInstruction(0x0012, -1, 0, 1, new short[]{(short) 0x0012}));
        // const/4 v1, 5 (offset 1) - loop counter
        instructions.add(new DexInstruction(0x5112, -1, 1, 1, new short[]{(short) 0x5112}));
        // const/4 v2, 1 (offset 2) - decrement step
        instructions.add(new DexInstruction(0x1212, -1, 2, 1, new short[]{(short) 0x1212}));
        
        // L_start (offset 3): if-eqz v1, +5 (target is offset 8)
        instructions.add(new DexInstruction(0x0138, -1, 3, 2, new short[]{(short) 0x0138, 5}));
        // add-int/2addr v0, v2 (offset 5) -> v0 += 1
        instructions.add(new DexInstruction(0x2090, -1, 5, 1, new short[]{(short) 0x2090}));
        // sub-int/2addr v1, v2 (offset 6) -> v1 -= 1
        instructions.add(new DexInstruction(0x2191, -1, 6, 1, new short[]{(short) 0x2191}));
        // goto -4 (target is L_start at offset 3)
        // rawOpcode: (-4 & 0xFF) << 8 | 0x28 = 0xFC28
        instructions.add(new DexInstruction(0xFC28, -1, 7, 1, new short[]{(short) 0xFC28}));
        
        // L_end (offset 8): return v0
        instructions.add(new DexInstruction(0x000F, -1, 8, 1, new short[]{(short) 0x000F}));

        Object result = emulator.execute(instructions, new HashMap<>());
        assertThat(result).isEqualTo(5);
    }

    @Test
    void testArrayOperations() {
        dexforge.core.parser.dex.service.DexFastIndexer indexer = org.mockito.Mockito.mock(dexforge.core.parser.dex.service.DexFastIndexer.class);
        dexforge.core.parser.dex.sections.DexTypePool typePool = org.mockito.Mockito.mock(dexforge.core.parser.dex.sections.DexTypePool.class);
        org.mockito.Mockito.when(indexer.getTypePool()).thenReturn(typePool);
        org.mockito.Mockito.when(typePool.getTypeName(99)).thenReturn("[B");

        SmaliEmulator emulator = new SmaliEmulator();
        emulator.setIndexer(indexer);

        List<DexInstruction> instructions = new ArrayList<>();

        // const/4 v1, 3
        instructions.add(new DexInstruction(0x3112, -1, 0, 1, new short[]{(short) 0x3112}));
        // new-array v0, v1, type@99
        instructions.add(new DexInstruction(0x1021, 99, 1, 2, new short[]{(short) 0x1021, 99}));
        // const/16 v2, 42
        instructions.add(new DexInstruction(0x0213, -1, 3, 2, new short[]{(short) 0x0213, 42}));
        // const/4 v3, 1
        instructions.add(new DexInstruction(0x1312, -1, 5, 1, new short[]{(short) 0x1312}));
        // aput v2, v0, v3
        instructions.add(new DexInstruction(0x024B, -1, 6, 2, new short[]{(short) 0x024B, 0x0300}));
        // aget v4, v0, v3
        instructions.add(new DexInstruction(0x0444, -1, 8, 2, new short[]{(short) 0x0444, 0x0300}));
        // return v4
        instructions.add(new DexInstruction(0x040F, -1, 10, 1, new short[]{(short) 0x040F}));

        Object result = emulator.execute(instructions, new HashMap<>());
        assertThat(result).isEqualTo((byte) 42);
    }
}
