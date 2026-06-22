package dexforge.plugins.input.java;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import dexforge.api.plugins.input.ICodeLoader;

import jadx.api.JadxArgs;
import jadx.api.JadxDecompiler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

class CustomLoadTest {

	private JadxDecompiler jadx;

	@TempDir
	Path tempDir;

	@BeforeEach
	void init() {
		jadx = new JadxDecompiler(new JadxArgs());
	}

	@AfterEach
	void close() {
		jadx.close();
	}

	@Test
	void loadFiles() {
		List<Path> files = Stream.of("HelloWorld.class", "HelloWorld$HelloInner.class")
				.map(this::getSample)
				.collect(Collectors.toList());
		ICodeLoader loadResult = JavaInputPlugin.loadClassFiles(files);
		loadDecompiler(loadResult);
		assertThat(jadx.getClassesWithInners())
				.hasSize(2)
				.satisfiesOnlyOnce(cls -> assertThat(cls.getName()).isEqualTo("HelloWorld"))
				.satisfiesOnlyOnce(cls -> assertThat(cls.getName()).isEqualTo("HelloInner"));
	}

	@Test
	void ignoreProfileEntriesInZip() throws IOException {
		Path zip = tempDir.resolve("sample.apk");
		try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(zip))) {
			addZipEntry(out, "HelloWorld.class", Files.readAllBytes(getSample("HelloWorld.class")));
			addZipEntry(out, "assets/dexopt/baseline.prof", new byte[] { 0x70, 0x72, 0x6f, 0x00 });
			addZipEntry(out, "assets/dexopt/baseline.profm", new byte[] { 0x70, 0x72, 0x6d, 0x00 });
		}

		List<JavaClassReader> readers = new JavaInputLoader().collectFiles(List.of(zip));

		assertThat(readers)
				.hasSize(1)
				.satisfiesOnlyOnce(reader -> assertThat(reader.getFileName()).isEqualTo("sample.apk:HelloWorld.class"));
	}

	@Test
	void loadFromInputStream() throws IOException {
		String fileName = "HelloWorld$HelloInner.class";
		try (InputStream in = Files.newInputStream(getSample(fileName))) {
			ICodeLoader loadResult = JavaInputPlugin.loadFromInputStream(in, fileName);
			loadDecompiler(loadResult);
			assertThat(jadx.getClassesWithInners())
					.hasSize(1)
					.satisfiesOnlyOnce(cls -> assertThat(cls.getName()).isEqualTo("HelloWorld$HelloInner"));

			System.out.println(jadx.getClassesWithInners().get(0).getCode());
		}
	}

	@Test
	void loadSingleClass() throws IOException {
		String fileName = "HelloWorld.class";
		byte[] content = Files.readAllBytes(getSample(fileName));
		ICodeLoader loadResult = JavaInputPlugin.loadSingleClass(content, fileName);
		loadDecompiler(loadResult);
		assertThat(jadx.getClassesWithInners())
				.hasSize(1)
				.satisfiesOnlyOnce(cls -> assertThat(cls.getName()).isEqualTo("HelloWorld"));

		System.out.println(jadx.getClassesWithInners().get(0).getCode());
	}

	@Test
	void load() {
		ICodeLoader loadResult = JavaInputPlugin.load(loader -> {
			List<JavaClassReader> inputs = new ArrayList<>(2);
			try {
				String hello = "HelloWorld.class";
				byte[] content = Files.readAllBytes(getSample(hello));
				inputs.add(loader.loadClass(content, hello));

				String helloInner = "HelloWorld$HelloInner.class";
				InputStream in = Files.newInputStream(getSample(helloInner));
				inputs.addAll(loader.loadInputStream(in, helloInner));
			} catch (Exception e) {
				fail(e);
			}
			return inputs;
		});
		loadDecompiler(loadResult);
		assertThat(jadx.getClassesWithInners())
				.hasSize(2)
				.satisfiesOnlyOnce(cls -> assertThat(cls.getName()).isEqualTo("HelloWorld"))
				.satisfiesOnlyOnce(cls -> {
					assertThat(cls.getName()).isEqualTo("HelloInner");
					assertThat(cls.getCode()).isEmpty(); // no code for moved inner class
				});

		assertThat(jadx.getClasses())
				.hasSize(1)
				.satisfiesOnlyOnce(cls -> assertThat(cls.getName()).isEqualTo("HelloWorld"))
				.satisfiesOnlyOnce(cls -> assertThat(cls.getInnerClasses()).hasSize(1)
						.satisfiesOnlyOnce(inner -> assertThat(inner.getName()).isEqualTo("HelloInner")));

		jadx.getClassesWithInners().forEach(cls -> System.out.println(cls.getCode()));
	}

	private static void addZipEntry(ZipOutputStream out, String name, byte[] content) throws IOException {
		out.putNextEntry(new ZipEntry(name));
		out.write(content);
		out.closeEntry();
	}

	public void loadDecompiler(ICodeLoader codeLoader) {
		try {
			jadx.addCustomCodeLoader(codeLoader);
			jadx.load();
		} catch (Exception e) {
			fail("Failed to load sample", e);
		}
	}

	public Path getSample(String name) {
		try {
			return Paths.get(ClassLoader.getSystemResource("samples/" + name).toURI());
		} catch (Exception e) {
			return fail("Failed to load sample", e);
		}
	}
}
