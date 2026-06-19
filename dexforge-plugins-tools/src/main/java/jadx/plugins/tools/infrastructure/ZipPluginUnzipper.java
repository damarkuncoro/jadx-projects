package jadx.plugins.tools.infrastructure;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import jadx.core.utils.exceptions.JadxRuntimeException;
import jadx.plugins.tools.domain.IPluginUnzipper;
import dexforge.zip.IZipEntry;
import dexforge.zip.ZipContent;
import dexforge.zip.ZipReader;

public class ZipPluginUnzipper implements IPluginUnzipper {
	@Override
	public void unzip(Path zipFile, Path outDir) {
		ZipReader zipReader = new ZipReader();
		try (ZipContent content = zipReader.open(zipFile.toFile())) {
			for (IZipEntry entry : content.getEntries()) {
				Path entryFile = outDir.resolve(entry.getName());
				Files.copy(entry.getInputStream(), entryFile, StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (IOException e) {
			throw new JadxRuntimeException("Failed to unzip file: " + zipFile, e);
		}
	}
}
