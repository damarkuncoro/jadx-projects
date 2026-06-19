package jadx.plugins.tools.domain;

import java.nio.file.Path;

import jadx.plugins.tools.data.JadxPluginMetadata;

public interface IPluginMetadataLoader {
	void fillMetadataFromPath(JadxPluginMetadata metadata, Path pluginPath) throws Exception;
}
