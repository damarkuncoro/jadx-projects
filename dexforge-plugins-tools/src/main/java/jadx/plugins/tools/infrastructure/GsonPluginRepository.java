package jadx.plugins.tools.infrastructure;

import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import jadx.plugins.tools.data.JadxInstalledPlugins;
import jadx.plugins.tools.domain.IPluginRepository;

import static jadx.core.utils.GsonUtils.buildGson;

public class GsonPluginRepository implements IPluginRepository {
	private final Path pluginsJsonPath;

	public GsonPluginRepository(Path pluginsJsonPath) {
		this.pluginsJsonPath = pluginsJsonPath;
	}

	@Override
	public JadxInstalledPlugins load() {
		if (!Files.isRegularFile(pluginsJsonPath)) {
			JadxInstalledPlugins plugins = new JadxInstalledPlugins();
			plugins.setVersion(1);
			return plugins;
		}
		try (Reader reader = Files.newBufferedReader(pluginsJsonPath, StandardCharsets.UTF_8)) {
			JadxInstalledPlugins data = buildGson().fromJson(reader, JadxInstalledPlugins.class);
			if (data.getVersion() == 0) {
				data.setVersion(1);
			}
			return data;
		} catch (Exception e) {
			throw new RuntimeException("Failed to read file: " + pluginsJsonPath, e);
		}
	}

	@Override
	public void save(JadxInstalledPlugins data) {
		if (data.getInstalled().isEmpty()) {
			try {
				Files.deleteIfExists(pluginsJsonPath);
			} catch (Exception e) {
				throw new RuntimeException("Failed to remove file: " + pluginsJsonPath, e);
			}
			return;
		}
		data.getInstalled().sort(null);
		try (Writer writer = Files.newBufferedWriter(pluginsJsonPath, StandardCharsets.UTF_8)) {
			buildGson().toJson(data, writer);
		} catch (Exception e) {
			throw new RuntimeException("Error saving file: " + pluginsJsonPath, e);
		}
	}
}
