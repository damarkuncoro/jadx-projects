package dexforge.core.parser.apk;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

class ApkSignerTest {

    @TempDir
    Path tempDir;

    @Test
    void testSignApk() throws Exception {
        File unsignedApk = tempDir.resolve("unsigned.apk").toFile();
        File signedApk = tempDir.resolve("signed.apk").toFile();

        // Create a dummy unsigned zip
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(unsignedApk))) {
            zos.putNextEntry(new ZipEntry("hello.txt"));
            zos.write("hello world".getBytes());
            zos.closeEntry();
        }

        ApkSigner signer = new ApkSigner();
        signer.sign(unsignedApk, signedApk);

        assertThat(signedApk).exists();

        // Verify signed ZIP contents
        try (ZipFile zip = new ZipFile(signedApk)) {
            // Check original file is present
            ZipEntry helloEntry = zip.getEntry("hello.txt");
            assertThat(helloEntry).isNotNull();
            try (InputStream is = zip.getInputStream(helloEntry)) {
                byte[] bytes = is.readAllBytes();
                assertThat(new String(bytes)).isEqualTo("hello world");
            }

            // Check Manifest is present and correct
            ZipEntry manifestEntry = zip.getEntry("META-INF/MANIFEST.MF");
            assertThat(manifestEntry).isNotNull();
            try (InputStream is = zip.getInputStream(manifestEntry)) {
                Manifest manifest = new Manifest(is);
                Attributes mainAttrs = manifest.getMainAttributes();
                assertThat(mainAttrs.getValue(Attributes.Name.MANIFEST_VERSION)).isEqualTo("1.0");

                Attributes helloAttrs = manifest.getAttributes("hello.txt");
                assertThat(helloAttrs).isNotNull();
                String digest = helloAttrs.getValue("SHA1-Digest");
                assertThat(digest).isNotBlank();
            }
        }
    }
}
