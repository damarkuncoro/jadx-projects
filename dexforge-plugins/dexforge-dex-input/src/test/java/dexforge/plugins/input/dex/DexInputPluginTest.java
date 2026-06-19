package dexforge.plugins.input.dex;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import dexforge.api.plugins.input.ICodeLoader;
import dexforge.api.plugins.input.data.AccessFlags;
import dexforge.api.plugins.input.data.AccessFlagsScope;
import dexforge.api.plugins.input.data.ICodeReader;
import dexforge.plugins.input.dex.utils.SmaliTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class DexInputPluginTest {

	@TempDir
	Path tempDir;

	@Test
	public void loadSampleApk() throws Exception {
		processFile(Paths.get(ClassLoader.getSystemResource("samples/app-with-fake-dex.apk").toURI()));
	}

	@Test
	public void loadHelloWorld() throws Exception {
		processFile(Paths.get(ClassLoader.getSystemResource("samples/hello.dex").toURI()));
	}

	@Test
	public void loadTestSmali() throws Exception {
		processFile(SmaliTestUtils.compileSmaliFromResource("samples/test.smali"));
	}

	@Test
	public void ignoreInvalidDexBuffer() throws Exception {
		byte[] invalidDex = new byte[112];
		invalidDex[0] = 'd';
		invalidDex[1] = 'e';
		invalidDex[2] = 'x';
		invalidDex[3] = '\n';

		try (ICodeLoader result = new DexInputPlugin().loadDex(invalidDex, "invalid.dex")) {
			assertThat(countClasses(result)).isZero();
		}
	}

	@Test
	public void ignoreInvalidDexEntryInZip() throws Exception {
		Path zip = tempDir.resolve("invalid-dex.apk");
		try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(zip))) {
			addZipEntry(out, "classes.dex", new byte[] { 0x12, 0x34, 0x56, 0x78 });
		}

		try (ICodeLoader result = new DexInputPlugin().loadFiles(List.of(zip))) {
			assertThat(countClasses(result)).isZero();
		}
	}

	private static void processFile(Path sample) throws IOException {
		System.out.println("Input file: " + sample.toAbsolutePath());
		long start = System.currentTimeMillis();
		List<Path> files = Collections.singletonList(sample);
		try (ICodeLoader result = new DexInputPlugin().loadFiles(files)) {
			AtomicInteger count = new AtomicInteger();
			result.visitClasses(cls -> {
				System.out.println();
				System.out.println("Class: " + cls.getType());
				System.out.println("AccessFlags: " + AccessFlags.format(cls.getAccessFlags(), AccessFlagsScope.CLASS));
				System.out.println("SuperType: " + cls.getSuperType());
				System.out.println("Interfaces: " + cls.getInterfacesTypes());
				System.out.println("Attributes: " + cls.getAttributes());
				count.getAndIncrement();

				cls.visitFieldsAndMethods(
						System.out::println,
						mth -> {
							System.out.println("---");
							System.out.println(mth);
							ICodeReader codeReader = mth.getCodeReader();
							if (codeReader != null) {
								codeReader.visitInstructions(insn -> {
									insn.decode();
									System.out.println(insn);
								});
							}
							System.out.println("---");
							System.out.println(mth.disassembleMethod());
							System.out.println("---");
						});
				System.out.println("----");
				System.out.println(cls.getDisassembledCode());
				System.out.println("----");
			});
			assertThat(count.get()).isGreaterThan(0);
		}
		System.out.println("Time: " + (System.currentTimeMillis() - start) + "ms");
	}

	private static int countClasses(ICodeLoader result) {
		AtomicInteger count = new AtomicInteger();
		result.visitClasses(cls -> count.getAndIncrement());
		return count.get();
	}

	private static void addZipEntry(ZipOutputStream out, String name, byte[] content) throws IOException {
		out.putNextEntry(new ZipEntry(name));
		out.write(content);
		out.closeEntry();
	}
}
