package jadx.gui.treemodel;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import jadx.api.ResourceFile;
import jadx.api.ResourceType;

import static org.assertj.core.api.Assertions.assertThat;

class DexResourceTest {

	@Test
	void testIsDexMagic(@TempDir File tempDir) throws IOException {
		// Test valid DEX header (dex\n035\0 with endian tag at offset 40)
		byte[] dexBytes = new byte[44];
		dexBytes[0] = 'd';
		dexBytes[1] = 'e';
		dexBytes[2] = 'x';
		dexBytes[3] = '\n';
		dexBytes[4] = '0';
		dexBytes[5] = '3';
		dexBytes[6] = '5';
		dexBytes[7] = 0;
		dexBytes[40] = 0x78;
		dexBytes[41] = 0x56;
		dexBytes[42] = 0x34;
		dexBytes[43] = 0x12;

		File dexFile = new File(tempDir, "test.txt");
		Files.write(dexFile.toPath(), dexBytes);

		ResourceFile rf = ResourceFile.createResourceFile(null, dexFile, ResourceType.UNKNOWN);
		JResource jRes = new JResource(rf, "test.txt", JResource.JResType.FILE);

		assertThat(jRes.isDexMagic()).isTrue();

		// Test invalid DEX header
		byte[] txtBytes = "hello world".getBytes();
		File txtFile = new File(tempDir, "hello.txt");
		Files.write(txtFile.toPath(), txtBytes);

		ResourceFile rf2 = ResourceFile.createResourceFile(null, txtFile, ResourceType.UNKNOWN);
		JResource jRes2 = new JResource(rf2, "hello.txt", JResource.JResType.FILE);

		assertThat(jRes2.isDexMagic()).isFalse();
	}
}
