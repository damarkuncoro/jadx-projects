package jadx.plugins.tools.application;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import dexforge.api.plugins.JadxPlugin;
import dexforge.api.plugins.JadxPluginInfo;

import jadx.core.utils.files.FileUtils;
import jadx.plugins.tools.JadxExternalPluginsLoader;
import jadx.plugins.tools.data.JadxPluginMetadata;
import jadx.plugins.tools.domain.IPluginRepository;

public class ListPluginsUseCase {
	private final IPluginRepository repository;
	private final Path installedDir;
	private final Path dropinsDir;

	public ListPluginsUseCase(IPluginRepository repository, Path installedDir, Path dropinsDir) {
		this.repository = repository;
		this.installedDir = installedDir;
		this.dropinsDir = dropinsDir;
	}

	public List<JadxPluginMetadata> getInstalled() {
		return repository.load().getInstalled();
	}

	public List<JadxPluginInfo> getAllPluginsInfo() {
		try (JadxExternalPluginsLoader pluginsLoader = new JadxExternalPluginsLoader()) {
			return pluginsLoader.load().stream()
					.map(JadxPlugin::getPluginInfo)
					.collect(Collectors.toList());
		}
	}

	public List<Path> getEnabledPluginPaths() {
		List<Path> list = new ArrayList<>();
		for (JadxPluginMetadata pluginMetadata : repository.load().getInstalled()) {
			if (pluginMetadata.isDisabled()) {
				continue;
			}
			list.add(installedDir.resolve(pluginMetadata.getPath()));
		}
		list.addAll(FileUtils.listFiles(dropinsDir));
		return list;
	}
}
