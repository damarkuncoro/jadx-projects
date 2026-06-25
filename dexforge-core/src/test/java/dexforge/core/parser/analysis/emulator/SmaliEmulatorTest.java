package dexforge.core.parser.analysis.emulator;

import static org.assertj.core.api.Assertions.assertThat;

import dexforge.core.parser.dex.model.DexInstruction;
import dexforge.core.parser.dex.model.DexCode;
import dexforge.core.parser.dex.sections.DexInstructionDecoder;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;

class SmaliEmulatorTest {

    @Test
    void testSequentialExecution() {
        SmaliEmulator emulator = new SmaliEmulator();
        short[] rawInsns = new short[] {
            (short) 0x5012, // const/4 v0, 5
            (short) 0x2112, // const/4 v1, 2
            (short) 0x10B0, // add-int/2addr v0, v1
            (short) 0x000F  // return v0
        };
        List<DexInstruction> instructions = DexInstructionDecoder.decode(
            new DexCode(5, 0, 0, 0, rawInsns, 0)
        );

        Object result = emulator.execute(instructions, new HashMap<>());
        assertThat(result).isEqualTo(7);
    }

    @Test
    void testGotoJump() {
        SmaliEmulator emulator = new SmaliEmulator();
        short[] rawInsns = new short[] {
            (short) 0x5012, // const/4 v0, 5
            (short) 0x0228, // goto +2 (skip next instruction)
            (short) 0x7012, // const/4 v0, 7 (should be skipped)
            (short) 0x000F  // return v0
        };
        List<DexInstruction> instructions = DexInstructionDecoder.decode(
            new DexCode(5, 0, 0, 0, rawInsns, 0)
        );

        Object result = emulator.execute(instructions, new HashMap<>());
        assertThat(result).isEqualTo(5);
    }

    @Test
    void testIfEqzBranch() {
        SmaliEmulator emulator = new SmaliEmulator();
        short[] rawInsns = new short[] {
            (short) 0x0012, // const/4 v0, 0
            (short) 0x0038, (short) 3, // if-eqz v0, +3
            (short) 0x7012, // const/4 v0, 7 (should be skipped)
            (short) 0x000F  // return v0
        };
        List<DexInstruction> instructions = DexInstructionDecoder.decode(
            new DexCode(5, 0, 0, 0, rawInsns, 0)
        );

        Object result = emulator.execute(instructions, new HashMap<>());
        assertThat(result).isEqualTo(0);
    }

    @Test
    void testIfNezBranch() {
        SmaliEmulator emulator = new SmaliEmulator();
        short[] rawInsns = new short[] {
            (short) 0x5012, // const/4 v0, 5
            (short) 0x0039, (short) 3, // if-nez v0, +3
            (short) 0x7012, // const/4 v0, 7 (should be skipped)
            (short) 0x000F  // return v0
        };
        List<DexInstruction> instructions = DexInstructionDecoder.decode(
            new DexCode(5, 0, 0, 0, rawInsns, 0)
        );

        Object result = emulator.execute(instructions, new HashMap<>());
        assertThat(result).isEqualTo(5);
    }

    @Test
    void testLoopExecution() {
        SmaliEmulator emulator = new SmaliEmulator();
        short[] rawInsns = new short[] {
            (short) 0x0012, // const/4 v0, 0 (offset 0) - accumulator
            (short) 0x5112, // const/4 v1, 5 (offset 1) - loop counter
            (short) 0x1212, // const/4 v2, 1 (offset 2) - decrement step
            
            // L_start (offset 3): if-eqz v1, +5 (target is return v0 at offset 8)
            (short) 0x0138, (short) 5,
            // add-int/2addr v0, v2 (offset 5) -> v0 += 1
            (short) 0x20B0,
            // sub-int/2addr v1, v2 (offset 6) -> v1 -= 1
            (short) 0x21B1,
            // goto -4 (target is L_start at offset 3)
            (short) 0xFC28,
            
            // L_end (offset 8): return v0
            (short) 0x000F
        };
        List<DexInstruction> instructions = DexInstructionDecoder.decode(
            new DexCode(5, 0, 0, 0, rawInsns, 0)
        );

        Object result = emulator.execute(instructions, new HashMap<>());
        assertThat(result).isEqualTo(5);
    }

    @Test
    void testArrayOperations() {
        dexforge.core.parser.dex.service.DexFastIndexer indexer = org.mockito.Mockito.mock(dexforge.core.parser.dex.service.DexFastIndexer.class);
        dexforge.core.parser.dex.sections.DexTypePool typePool = org.mockito.Mockito.mock(dexforge.core.parser.dex.sections.DexTypePool.class);
        org.mockito.Mockito.when(indexer.getTypePool()).thenReturn(typePool);
        org.mockito.Mockito.when(typePool.getTypeName(99)).thenReturn("[B");
        org.mockito.Mockito.when(typePool.getSize()).thenReturn(100);

        SmaliEmulator emulator = new SmaliEmulator();
        emulator.setIndexer(indexer);

        short[] rawInsns = new short[] {
            (short) 0x3112, // const/4 v1, 3
            (short) 0x1023, (short) 99, // new-array v0, v1, type@99
            (short) 0x0213, (short) 42, // const/16 v2, 42
            (short) 0x1312, // const/4 v3, 1
            (short) 0x024B, (short) 0x0300, // aput v2, v0, v3
            (short) 0x0444, (short) 0x0300, // aget v4, v0, v3
            (short) 0x040F  // return v4
        };
        List<DexInstruction> instructions = DexInstructionDecoder.decode(
            new DexCode(5, 0, 0, 0, rawInsns, 0)
        );

        Object result = emulator.execute(instructions, new HashMap<>());
        assertThat(result).isEqualTo((byte) 42);
    }
}
