package dexforge.core.parser.analysis.deobf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import dexforge.core.parser.dex.model.DexClass;
import dexforge.core.parser.dex.model.DexCode;
import dexforge.core.parser.dex.model.DexEncodedMethod;
import dexforge.core.parser.dex.sections.DexClassDataParser;
import dexforge.core.parser.dex.sections.DexCodeParser;
import dexforge.core.parser.dex.sections.DexMethodPool;
import dexforge.core.parser.dex.sections.DexStringPool;
import dexforge.core.parser.dex.service.DexFastIndexer;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

class DeobfuscationPatcherTest {

	@Test
	void testEndToEndPatching() {
		// 1. Setup Mock Indexer and Pools
		DexFastIndexer indexer = mock(DexFastIndexer.class);
		DexMethodPool methodPool = mock(DexMethodPool.class);
		DexStringPool stringPool = mock(DexStringPool.class);
		DexCodeParser codeParser = mock(DexCodeParser.class);

		when(indexer.getMethodPool()).thenReturn(methodPool);
		when(indexer.getStringPool()).thenReturn(stringPool);
		when(indexer.getCodeParser()).thenReturn(codeParser);

		// 2. Mock Obfuscated Method: Lo/zzmt;->b(I)Ljava/lang/String;
		String targetSig = "Lo/zzmt;->b(I)Ljava/lang/String;";
		when(methodPool.getSize()).thenReturn(100);
		when(methodPool.getMethodSignature(5)).thenReturn(targetSig);
		when(methodPool.getMethodName(5)).thenReturn("b");

		// 3. Mock Call Site Method
		String callerClass = "Lcom/example/MyActivity;";
		String callerMethodName = "onCreate";
		String callerSig = callerClass + "->" + callerMethodName + "(Landroid/os/Bundle;)V";
		when(methodPool.getMethodName(1)).thenReturn(callerMethodName);
		when(methodPool.getMethodSignature(1)).thenReturn(callerSig);

		// 4. Create Obfuscated Bytecode
		// const/4 v0, 39028 (0x9874) -> let's use 1 for simplicity in const/4
		// const/4 v0, 1 -> 0x1012
		// invoke-static {v0}, Lo/zzmt;->b(I) -> 0x1071, 0x0005, 0x0000
		short[] insns = new short[] {
				(short) 0x1012, // offset 0
				(short) 0x1071, // offset 1 (2 bytes)
				(short) 0x0005, // offset 2
				(short) 0x0000  // offset 3
		};
		DexCode code = new DexCode(1, 0, 0, 0, insns, 0);
		when(codeParser.parse(200)).thenReturn(code);

		// 5. Mock Class Structure
		DexEncodedMethod method = new DexEncodedMethod(1, 1, 200);
		DexClassDataParser.ClassData classData = new DexClassDataParser.ClassData(
				Collections.emptyList(), Collections.emptyList(),
				Collections.singletonList(method), Collections.emptyList());
		DexClass clazz = new DexClass(callerClass, 1, "Landroid/app/Activity;", Collections.emptyList(), "MyActivity.java", 100, 0, 0);
		clazz.setClassData(classData);
		when(indexer.getClasses()).thenReturn(Collections.singletonList(clazz));

		// 6. Run Patcher
		DeobfuscationPatcher patcher = new DeobfuscationPatcher(indexer, Collections.singletonList(indexer), null);
		Map<String, String> patchedClasses = patcher.patchAll();

		// 7. Verify Results
		assertThat(patchedClasses).containsKey(callerClass);
		String smali = patchedClasses.get(callerClass);

		// The original invoke-static at offset 1 should be replaced with patched const-string
		assertThat(smali).contains("const-string v0, \"DECODED_STR_1\" # patched");
		assertThat(smali).doesNotContain("invoke-static v0, Lo/zzmt;->b(I)Ljava/lang/String;");

		System.out.println("Generated Patched Smali:\n" + smali);
	}
}
