package jadx.plugins.tools.application;

import jadx.plugins.tools.data.JadxInstalledPlugins;
import jadx.plugins.tools.data.JadxPluginMetadata;
import jadx.plugins.tools.domain.IPluginRepository;

public class PluginStatusUseCase {
	private final IPluginRepository repository;

	public PluginStatusUseCase(IPluginRepository repository) {
		this.repository = repository;
	}

	public boolean changeDisabledStatus(String pluginId, boolean disabled) {
		JadxInstalledPlugins data = repository.load();
		JadxPluginMetadata plugin = data.getInstalled().stream()
				.filter(p -> p.getPluginId().equals(pluginId))
				.findFirst()
				.orElseThrow(() -> new RuntimeException("Plugin not found: " + pluginId));
		if (plugin.isDisabled() == disabled) {
			return false;
		}
		plugin.setDisabled(disabled);
		data.setUpdated(System.currentTimeMillis());
		repository.save(data);
		return true;
	}
}
