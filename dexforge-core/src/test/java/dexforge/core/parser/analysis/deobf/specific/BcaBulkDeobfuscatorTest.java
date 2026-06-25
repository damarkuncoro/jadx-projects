package dexforge.core.parser.analysis.deobf.specific;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import dexforge.core.parser.analysis.deobf.DeobfuscationEngine;
import dexforge.core.parser.analysis.deobf.specific.model.DecryptedString;
import dexforge.core.parser.dex.model.DexClass;
import dexforge.core.parser.dex.model.DexCode;
import dexforge.core.parser.dex.model.DexEncodedMethod;
import dexforge.core.parser.dex.sections.DexClassDataParser;
import dexforge.core.parser.dex.sections.DexCodeParser;
import dexforge.core.parser.dex.sections.DexMethodPool;
import dexforge.core.parser.dex.service.DexFastIndexer;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class BcaBulkDeobfuscatorTest {

	@Test
	void testDeobfuscationSuccess() {
		// Setup mocks
		DexFastIndexer indexer = mock(DexFastIndexer.class);
		DexMethodPool methodPool = mock(DexMethodPool.class);
		DexCodeParser codeParser = mock(DexCodeParser.class);

		when(indexer.getMethodPool()).thenReturn(methodPool);
		when(indexer.getCodeParser()).thenReturn(codeParser);

		// Mock a class and method
		DexClass clazz = new DexClass("Lcom/example/Test;", 1, "Ljava/lang/Object;", Collections.emptyList(), "Test.java", 100, 0, 0);
		DexEncodedMethod method = new DexEncodedMethod(1, 1, 200);
		DexClassDataParser.ClassData classData = new DexClassDataParser.ClassData(
				Collections.emptyList(), Collections.emptyList(),
				Collections.singletonList(method), Collections.emptyList());
		clazz.setClassData(classData);

		when(indexer.getClasses()).thenReturn(Collections.singletonList(clazz));
		when(methodPool.getSize()).thenReturn(100);
		when(methodPool.getMethodSignature(anyInt())).thenReturn("Lo/zzmt;->b(I)Ljava/lang/String;");
		when(methodPool.getMethodName(anyInt())).thenReturn("testMethod");

		short[] insns = new short[] {
				(short) 0x1012, // const/4 v0, 1
				(short) 0x1071, // invoke-static {v0}, ...
				(short) 0x0005,
				(short) 0x0000
		};

		DexCode code = new DexCode(1, 0, 0, 0, insns, 0);
		when(codeParser.parse(200)).thenReturn(code);
		when(methodPool.getMethodSignature(5)).thenReturn("Lo/zzmt;->b(I)Ljava/lang/String;");
		when(methodPool.getMethodSignature(1)).thenReturn("Lcom/example/Test;->testMethod()V");

		// Run using the new Scalable Engine
		DeobfuscationEngine engine = new DeobfuscationEngine(indexer);
		BcaBulkDeobfuscator bcaModule = new BcaBulkDeobfuscator(indexer, null);
		engine.registerModule(bcaModule);
		engine.run();

		// Verify findings
		List<DecryptedString> findings = bcaModule.getFindings();
		assertThat(findings).hasSize(1);
		assertThat(findings.get(0).getId()).isEqualTo(1);
		assertThat(findings.get(0).getValue()).isEqualTo("DECODED_STR_1");
	}

	@Test
	void testRegisterClobbering() {
		// Setup mocks
		DexFastIndexer indexer = mock(DexFastIndexer.class);
		DexMethodPool methodPool = mock(DexMethodPool.class);
		DexCodeParser codeParser = mock(DexCodeParser.class);

		when(indexer.getMethodPool()).thenReturn(methodPool);
		when(indexer.getCodeParser()).thenReturn(codeParser);

		DexClass clazz = new DexClass("Lcom/example/Test;", 1, "Ljava/lang/Object;", Collections.emptyList(), "Test.java", 100, 0, 0);
		DexEncodedMethod method = new DexEncodedMethod(1, 1, 200);
		DexClassDataParser.ClassData classData = new DexClassDataParser.ClassData(
				Collections.emptyList(), Collections.emptyList(),
				Collections.singletonList(method), Collections.emptyList());
		clazz.setClassData(classData);

		when(indexer.getClasses()).thenReturn(Collections.singletonList(clazz));
		when(methodPool.getSize()).thenReturn(100);

		// Instructions:
		// const/4 v0, 1
		// move v0, v1 (v0 is clobbered because v1 has no constant value)
		// invoke-static {v0}, Lo/zzmt;->b(I)
		short[] insns = new short[] {
				(short) 0x1012, // const/4 v0, 1
				(short) 0x1001, // move v0, v1
				(short) 0x1071, // invoke-static {v0}, ...
				(short) 0x0005,
				(short) 0x0000
		};

		DexCode code = new DexCode(2, 0, 0, 0, insns, 0);
		when(codeParser.parse(200)).thenReturn(code);
		when(methodPool.getMethodSignature(5)).thenReturn("Lo/zzmt;->b(I)Ljava/lang/String;");
		when(methodPool.getMethodName(anyInt())).thenReturn("testMethod");

		DeobfuscationEngine engine = new DeobfuscationEngine(indexer);
		BcaBulkDeobfuscator bcaModule = new BcaBulkDeobfuscator(indexer);
		engine.registerModule(bcaModule);
		engine.run();

		// Should NOT find anything because v0 was clobbered
		List<DecryptedString> findings = bcaModule.getFindings();
		assertThat(findings).isEmpty();
	}
}
