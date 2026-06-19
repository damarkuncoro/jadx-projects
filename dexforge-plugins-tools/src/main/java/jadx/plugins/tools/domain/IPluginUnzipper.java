package jadx.plugins.tools.domain;

import java.nio.file.Path;

public interface IPluginUnzipper {
	void unzip(Path zipFile, Path outDir) throws Exception;
}
