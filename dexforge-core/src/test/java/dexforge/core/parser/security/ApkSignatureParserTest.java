package dexforge.core.parser.security;

import static org.assertj.core.api.Assertions.assertThat;

import dexforge.core.parser.security.model.ApkSignatureInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

class ApkSignatureParserTest {

    @TempDir
    Path tempDir;

    @Test
    void testParseV1NoSignature() throws Exception {
        File apkFile = tempDir.resolve("no_sig.apk").toFile();

        // Create a ZIP without signatures
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(apkFile))) {
            zos.putNextEntry(new ZipEntry("classes.dex"));
            zos.write(new byte[]{1, 2, 3});
            zos.closeEntry();
        }

        ApkSignatureParser parser = new ApkSignatureParser();
        ApkSignatureInfo info = parser.parseV1(apkFile);

        assertThat(info).isNotNull();
        assertThat(info.getSchemeVersion()).isEqualTo(1);
        assertThat(info.getCertificates()).isEmpty();
    }

    @Test
    void testParseV1CorruptedSignature() throws Exception {
        File apkFile = tempDir.resolve("corrupted_sig.apk").toFile();

        // Create a ZIP with a corrupted .RSA file
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(apkFile))) {
            zos.putNextEntry(new ZipEntry("META-INF/CERT.RSA"));
            zos.write("corrupted certificate content".getBytes());
            zos.closeEntry();
        }

        ApkSignatureParser parser = new ApkSignatureParser();
        ApkSignatureInfo info = parser.parseV1(apkFile);

        // Exceptions should be caught and default info returned
        assertThat(info).isNotNull();
        assertThat(info.getSchemeVersion()).isEqualTo(1);
        assertThat(info.getCertificates()).isEmpty();
    }

    @Test
    void testParseNonExistentFile() {
        File nonExistent = new File("does_not_exist.apk");
        ApkSignatureParser parser = new ApkSignatureParser();
        ApkSignatureInfo info = parser.parseV1(nonExistent);

        assertThat(info).isNotNull();
        assertThat(info.getSchemeVersion()).isEqualTo(1);
        assertThat(info.getCertificates()).isEmpty();
    }
}
