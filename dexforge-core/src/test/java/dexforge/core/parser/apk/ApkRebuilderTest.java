package dexforge.core.parser.apk;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

class ApkRebuilderTest {

    @TempDir
    Path tempDir;

    @Test
    void testRebuildApk() throws Exception {
        Path sourceDir = tempDir.resolve("source");
        Files.createDirectories(sourceDir);

        // Create some source files
        Path file1 = sourceDir.resolve("file1.txt");
        Files.write(file1, "content1".getBytes());

        Path subDir = sourceDir.resolve("sub");
        Files.createDirectories(subDir);
        Path file2 = subDir.resolve("file2.txt");
        Files.write(file2, "content2".getBytes());

        File outputApk = tempDir.resolve("rebuilt.apk").toFile();

        ApkRebuilder rebuilder = new ApkRebuilder();
        rebuilder.rebuild(outputApk, sourceDir.toFile());

        assertThat(outputApk).exists();

        // Read ZIP contents and assert presence of the files
        try (ZipFile zip = new ZipFile(outputApk)) {
            List<? extends ZipEntry> entries = Collections.list(zip.entries());
            assertThat(entries).hasSize(2);

            ZipEntry entry1 = zip.getEntry("file1.txt");
            assertThat(entry1).isNotNull();
            try (InputStream is = zip.getInputStream(entry1)) {
                byte[] bytes = is.readAllBytes();
                assertThat(new String(bytes)).isEqualTo("content1");
            }

            ZipEntry entry2 = zip.getEntry("sub/file2.txt");
            assertThat(entry2).isNotNull();
            try (InputStream is = zip.getInputStream(entry2)) {
                byte[] bytes = is.readAllBytes();
                assertThat(new String(bytes)).isEqualTo("content2");
            }
        }
    }
}
