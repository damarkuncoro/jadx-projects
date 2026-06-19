package jadx.plugins.tools.domain;

import jadx.plugins.tools.data.JadxInstalledPlugins;

public interface IPluginRepository {
	JadxInstalledPlugins load();

	void save(JadxInstalledPlugins data);
}
