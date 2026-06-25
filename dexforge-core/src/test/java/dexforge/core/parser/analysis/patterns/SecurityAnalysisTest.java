package dexforge.core.parser.analysis.patterns;

import dexforge.core.parser.dex.model.DexClass;
import dexforge.core.parser.dex.sections.DexClassDataParser.ClassData;
import dexforge.core.parser.dex.model.DexCode;
import dexforge.core.parser.dex.model.DexEncodedMethod;
import dexforge.core.parser.dex.sections.DexMethodPool;
import dexforge.core.parser.dex.sections.DexCodeParser;
import dexforge.core.parser.dex.service.DexFastIndexer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SecurityAnalysisTest {
    private DexFastIndexer indexer;
    private DexCodeParser codeParser;
    private DexMethodPool methodPool;

    @BeforeEach
    void setUp() {
        indexer = mock(DexFastIndexer.class);
        codeParser = mock(DexCodeParser.class);
        methodPool = mock(DexMethodPool.class);

        when(indexer.getCodeParser()).thenReturn(codeParser);
        when(indexer.getMethodPool()).thenReturn(methodPool);
    }

    @Test
    @DisplayName("ByteArrayDecoder should detect ASCII key arrays from fill-array-data")
    void testByteArrayDecoder() {
        DexClass clazz = mock(DexClass.class);
        DexEncodedMethod method = mock(DexEncodedMethod.class);
        when(method.getCodeOff()).thenReturn(100);
        when(method.getMethodIndex()).thenReturn(0);

        ClassData classData = new ClassData(
            Collections.emptyList(), Collections.emptyList(), List.of(method), Collections.emptyList()
        );
        clazz.setClassData(classData);
        when(clazz.getClassData()).thenReturn(classData);

        short[] codeUnits = new short[30];
        codeUnits[0] = 0x0026;
        codeUnits[1] = 10;
        codeUnits[2] = 0;
        codeUnits[10] = (short) 0x0300;
        codeUnits[11] = 1;
        codeUnits[12] = 16;
        codeUnits[13] = 0;
        
        byte[] keyBytes = "w0w_g0od_j0b_wbm".getBytes();
        for (int i = 0; i < 16; i++) {
            int wordIdx = 10 + 4 + (i / 2);
            int byteShift = (i % 2) * 8;
            codeUnits[wordIdx] |= (short) ((keyBytes[i] & 0xFF) << byteShift);
        }

        DexCode dexCode = mock(DexCode.class);
        when(dexCode.getInstructions()).thenReturn(codeUnits);
        when(codeParser.parse(100)).thenReturn(dexCode);

        when(indexer.getClasses()).thenReturn(List.of(clazz));
        when(methodPool.getMethodSignature(0)).thenReturn("Lfoo/Bar;->getKey()V");

        ByteArrayDecoder decoder = new ByteArrayDecoder(indexer);
        List<ByteArrayDecoder.DecodedArray> decoded = decoder.decodeStaticArrays();

        assertEquals(1, decoded.size());
        assertEquals("Lfoo/Bar;->getKey()V", decoded.get(0).getMethodSignature());
        assertEquals("w0w_g0od_j0b_wbm", decoded.get(0).getAscii());
        assertEquals(16, decoded.get(0).getSize());
    }

    @Test
    @DisplayName("SecurityTrapDetector should detect divide-by-zero traps")
    void testSecurityTrapDetector() {
        DexClass clazz = mock(DexClass.class);
        DexEncodedMethod method = mock(DexEncodedMethod.class);
        when(method.getCodeOff()).thenReturn(200);
        when(method.getMethodIndex()).thenReturn(1);

        ClassData classData = new ClassData(
            Collections.emptyList(), Collections.emptyList(), List.of(method), Collections.emptyList()
        );
        clazz.setClassData(classData);
        when(clazz.getClassData()).thenReturn(classData);

        // div-int/lit8 v0, v1, 0 (opcode = 0xDB)
        short[] codeUnits = {0x00DB, 0x0001};

        DexCode dexCode = mock(DexCode.class);
        when(dexCode.getInstructions()).thenReturn(codeUnits);
        when(codeParser.parse(200)).thenReturn(dexCode);

        when(indexer.getClasses()).thenReturn(List.of(clazz));
        when(methodPool.getMethodSignature(1)).thenReturn("Lfoo/Bar;->trapCheck()V");

        SecurityTrapDetector detector = new SecurityTrapDetector(indexer);
        List<SecurityTrapDetector.TrapWarning> warnings = detector.scanForTraps();

        assertEquals(1, warnings.size());
        assertEquals("Lfoo/Bar;->trapCheck()V", warnings.get(0).getMethodSignature());
        assertTrue(warnings.get(0).getDescription().contains("divide-by-zero"));
    }
}
