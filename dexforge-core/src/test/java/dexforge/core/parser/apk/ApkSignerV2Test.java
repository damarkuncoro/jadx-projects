package dexforge.core.parser.apk;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

class ApkSignerV2Test {

    @TempDir
    Path tempDir;

    @Test
    void testSignApkBuilderV2() throws Exception {
        File apkFile = tempDir.resolve("test_v2.apk").toFile();

        // 1. Create a dummy unsigned zip (which is just a zip with no comments, so EOCD is 22 bytes)
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(apkFile))) {
            zos.putNextEntry(new ZipEntry("dummy.txt"));
            zos.write("some dummy data".getBytes());
            zos.closeEntry();
        }

        long originalLength = apkFile.length();

        // 2. Sign the APK using ApkSignerV2
        ApkSignerV2 signer = new ApkSignerV2();
        signer.sign(apkFile);

        // 3. Verify the signed APK size increased
        assertThat(apkFile.length()).isGreaterThan(originalLength);

        // 4. Verify that the file contains the APK signature block magic bytes
        // Magic values:
        // APK_SIG_BLOCK_MAGIC_LO = 0x20676953204b5041L ("APK Sig ")
        // APK_SIG_BLOCK_MAGIC_HI = 0x3234206b636f6c42L ("Block 42")
        try (RandomAccessFile raf = new RandomAccessFile(apkFile, "r")) {
            byte[] fileBytes = new byte[(int) raf.length()];
            raf.readFully(fileBytes);

            String fileString = new String(fileBytes);
            assertThat(fileString).contains("APK Sig ");
            assertThat(fileString).contains("Block 42");
        }
    }
}
