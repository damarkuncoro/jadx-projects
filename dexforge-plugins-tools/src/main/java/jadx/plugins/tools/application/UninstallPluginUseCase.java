package jadx.plugins.tools.application;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import jadx.core.utils.files.FileUtils;
import jadx.plugins.tools.data.JadxInstalledPlugins;
import jadx.plugins.tools.data.JadxPluginMetadata;
import jadx.plugins.tools.domain.IPluginRepository;

public class UninstallPluginUseCase {
	private final IPluginRepository repository;
	private final Path installedDir;

	public UninstallPluginUseCase(IPluginRepository repository, Path installedDir) {
		this.repository = repository;
		this.installedDir = installedDir;
	}

	public boolean uninstall(String pluginId) {
		JadxInstalledPlugins plugins = repository.load();
		Optional<JadxPluginMetadata> found = plugins.getInstalled().stream()
				.filter(p -> p.getPluginId().equals(pluginId))
				.findFirst();
		if (found.isEmpty()) {
			return false;
		}
		JadxPluginMetadata plugin = found.get();
		deletePlugin(plugin);
		plugins.getInstalled().remove(plugin);
		repository.save(plugins);
		return true;
	}

	private void deletePlugin(JadxPluginMetadata plugin) {
		try {
			Path pluginPath = installedDir.resolve(plugin.getPath());
			if (Files.isDirectory(pluginPath)) {
				FileUtils.deleteDir(pluginPath);
			} else {
				Files.deleteIfExists(pluginPath);
			}
		} catch (IOException e) {
			// ignore
		}
	}
}
