package jadx.plugins.tools.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jadx.plugins.tools.data.JadxInstalledPlugins;
import jadx.plugins.tools.data.JadxPluginMetadata;
import jadx.plugins.tools.data.JadxPluginUpdate;
import jadx.plugins.tools.domain.IPluginRepository;
import jadx.plugins.tools.resolvers.IJadxPluginResolver;
import jadx.plugins.tools.resolvers.ResolversRegistry;

public class UpdatePluginsUseCase {
	private static final Logger LOG = LoggerFactory.getLogger(UpdatePluginsUseCase.class);

	private final IPluginRepository repository;
	private final InstallPluginUseCase installUseCase;

	public UpdatePluginsUseCase(IPluginRepository repository, InstallPluginUseCase installUseCase) {
		this.repository = repository;
		this.installUseCase = installUseCase;
	}

	public List<JadxPluginUpdate> updateAll() {
		JadxInstalledPlugins plugins = repository.load();
		int size = plugins.getInstalled().size();
		List<JadxPluginUpdate> updates = new ArrayList<>(size);
		List<JadxPluginMetadata> newList = new ArrayList<>(size);
		for (JadxPluginMetadata plugin : plugins.getInstalled()) {
			JadxPluginMetadata newVersion = null;
			try {
				newVersion = update(plugin);
			} catch (Exception e) {
				LOG.warn("Failed to update plugin: {}", plugin.getPluginId(), e);
			}
			if (newVersion != null) {
				updates.add(new JadxPluginUpdate(plugin, newVersion));
				newList.add(newVersion);
			} else {
				newList.add(plugin);
			}
		}
		if (!updates.isEmpty()) {
			plugins.setUpdated(System.currentTimeMillis());
			plugins.setInstalled(newList);
			repository.save(plugins);
		}
		return updates;
	}

	public Optional<JadxPluginUpdate> update(String pluginId) {
		JadxInstalledPlugins plugins = repository.load();
		JadxPluginMetadata plugin = plugins.getInstalled().stream()
				.filter(p -> p.getPluginId().equals(pluginId))
				.findFirst()
				.orElseThrow(() -> new RuntimeException("Plugin not found: " + pluginId));

		JadxPluginMetadata newVersion = update(plugin);
		if (newVersion == null) {
			return Optional.empty();
		}
		plugins.setUpdated(System.currentTimeMillis());
		plugins.getInstalled().remove(plugin);
		plugins.getInstalled().add(newVersion);
		repository.save(plugins);
		return Optional.of(new JadxPluginUpdate(plugin, newVersion));
	}

	private JadxPluginMetadata update(JadxPluginMetadata plugin) {
		IJadxPluginResolver resolver = ResolversRegistry.getResolver(plugin.getLocationId());
		if (!resolver.isUpdateSupported()) {
			return null;
		}
		Optional<JadxPluginMetadata> updateOpt = resolver.resolve(plugin.getLocationId());
		if (updateOpt.isEmpty()) {
			return null;
		}
		JadxPluginMetadata update = updateOpt.get();
		if (Objects.equals(update.getVersion(), plugin.getVersion())) {
			return null;
		}
		installUseCase.fillMetadata(update);
		installUseCase.install(update);
		return update;
	}
}
