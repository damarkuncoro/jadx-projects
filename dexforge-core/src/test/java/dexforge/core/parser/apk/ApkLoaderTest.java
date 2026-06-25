package dexforge.core.parser.apk;

import static org.assertj.core.api.Assertions.assertThat;

import dexforge.core.parser.dex.service.DexProject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

class ApkLoaderTest {

    @TempDir
    Path tempDir;

    @Test
    void testLoadApkWithMinimalFiles() throws Exception {
        File apkFile = tempDir.resolve("test_app.apk").toFile();

        // Create a minimal 112-byte DEX header
        ByteBuffer dexBuffer = ByteBuffer.allocate(112).order(ByteOrder.LITTLE_ENDIAN);
        dexBuffer.put("dex\n035\0".getBytes()); // Magic
        dexBuffer.putInt(0);                    // Checksum
        dexBuffer.put(new byte[20]);             // Signature
        dexBuffer.putInt(112);                  // File Size
        dexBuffer.putInt(112);                  // Header Size
        dexBuffer.putInt(0x12345678);           // Endian Tag
        // Leave all other sizes and offsets as 0

        // Create mock manifest, resources, native library, and layout bytes
        byte[] mockManifest = new byte[]{0, 0, 0, 0}; // returns from parsing immediately
        byte[] mockResources = new byte[]{0, 0};      // returns from parsing immediately
        byte[] mockElf = new byte[]{0, 0, 0, 0};      // returns from parsing immediately
        byte[] mockLayout = new byte[]{0, 0, 0, 0};    // returns from parsing immediately

        // Write these to a ZipFile (APK)
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(apkFile))) {
            zos.putNextEntry(new ZipEntry("classes.dex"));
            zos.write(dexBuffer.array());
            zos.closeEntry();

            zos.putNextEntry(new ZipEntry("AndroidManifest.xml"));
            zos.write(mockManifest);
            zos.closeEntry();

            zos.putNextEntry(new ZipEntry("resources.arsc"));
            zos.write(mockResources);
            zos.closeEntry();

            zos.putNextEntry(new ZipEntry("lib/armeabi-v7a/libnative.so"));
            zos.write(mockElf);
            zos.closeEntry();

            zos.putNextEntry(new ZipEntry("res/layout/activity_main.xml"));
            zos.write(mockLayout);
            zos.closeEntry();
        }

        ApkLoader loader = new ApkLoader();
        DexProject project = loader.load(apkFile);

        assertThat(project).isNotNull();
        assertThat(project.getIndexers()).hasSize(1);
        assertThat(loader.getIndexers()).hasSize(1);
        assertThat(loader.getResourceResolver()).isNotNull();
        assertThat(loader.getActivities()).isEmpty();
        assertThat(loader.getPermissions()).isEmpty();
        assertThat(loader.getLayouts()).isEmpty();
        assertThat(loader.getJniBridges()).isEmpty();
    }
}
