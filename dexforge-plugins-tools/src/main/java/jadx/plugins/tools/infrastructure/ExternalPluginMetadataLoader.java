package jadx.plugins.tools.infrastructure;

import java.nio.file.Path;

import dexforge.api.plugins.JadxPlugin;
import dexforge.api.plugins.JadxPluginInfo;

import jadx.plugins.tools.JadxExternalPluginsLoader;
import jadx.plugins.tools.data.JadxPluginMetadata;
import jadx.plugins.tools.domain.IPluginMetadataLoader;

public class ExternalPluginMetadataLoader implements IPluginMetadataLoader {
	@Override
	public void fillMetadataFromPath(JadxPluginMetadata metadata, Path pluginPath) {
		try (JadxExternalPluginsLoader loader = new JadxExternalPluginsLoader()) {
			JadxPlugin jadxPlugin = loader.loadFromPath(pluginPath);
			JadxPluginInfo pluginInfo = jadxPlugin.getPluginInfo();
			metadata.setPluginId(pluginInfo.getPluginId());
			metadata.setName(pluginInfo.getName());
			metadata.setDescription(pluginInfo.getDescription());
			metadata.setHomepage(pluginInfo.getHomepage());
			metadata.setRequiredJadxVersion(pluginInfo.getRequiredJadxVersion());
		} catch (NoSuchMethodError e) {
			throw new RuntimeException("Looks like plugin uses unknown API, try to update jadx version", e);
		}
	}
}
