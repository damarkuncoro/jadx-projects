package jadx.plugins.tools;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import dexforge.api.plugins.JadxPluginInfo;

import jadx.plugins.tools.application.InstallPluginUseCase;
import jadx.plugins.tools.application.ListPluginsUseCase;
import jadx.plugins.tools.application.PluginStatusUseCase;
import jadx.plugins.tools.application.UninstallPluginUseCase;
import jadx.plugins.tools.application.UpdatePluginsUseCase;
import jadx.plugins.tools.data.JadxPluginMetadata;
import jadx.plugins.tools.data.JadxPluginUpdate;
import jadx.plugins.tools.domain.IPluginDownloader;
import jadx.plugins.tools.domain.IPluginMetadataLoader;
import jadx.plugins.tools.domain.IPluginRepository;
import jadx.plugins.tools.domain.IPluginUnzipper;
import jadx.plugins.tools.infrastructure.ExternalPluginMetadataLoader;
import jadx.plugins.tools.infrastructure.GsonPluginRepository;
import jadx.plugins.tools.infrastructure.HttpPluginDownloader;
import jadx.plugins.tools.infrastructure.ZipPluginUnzipper;

import static jadx.plugins.tools.utils.PluginFiles.DROPINS_DIR;
import static jadx.plugins.tools.utils.PluginFiles.INSTALLED_DIR;
import static jadx.plugins.tools.utils.PluginFiles.PLUGINS_JSON;

public class JadxPluginsTools {
	private static final JadxPluginsTools INSTANCE = new JadxPluginsTools();

	public static JadxPluginsTools getInstance() {
		return INSTANCE;
	}

	private final InstallPluginUseCase installUseCase;
	private final UninstallPluginUseCase uninstallUseCase;
	private final UpdatePluginsUseCase updateUseCase;
	private final PluginStatusUseCase statusUseCase;
	private final ListPluginsUseCase listUseCase;

	private JadxPluginsTools() {
		// 1. Instantiate Infrastructure Adapters
		IPluginRepository repository = new GsonPluginRepository(PLUGINS_JSON);
		IPluginDownloader downloader = new HttpPluginDownloader();
		IPluginUnzipper unzipper = new ZipPluginUnzipper();
		IPluginMetadataLoader metadataLoader = new ExternalPluginMetadataLoader();

		// 2. Instantiate Use Cases (wiring dependencies)
		this.uninstallUseCase = new UninstallPluginUseCase(repository, INSTALLED_DIR);
		this.installUseCase = new InstallPluginUseCase(repository, downloader, unzipper, metadataLoader, INSTALLED_DIR, uninstallUseCase);
		this.updateUseCase = new UpdatePluginsUseCase(repository, installUseCase);
		this.statusUseCase = new PluginStatusUseCase(repository);
		this.listUseCase = new ListPluginsUseCase(repository, INSTALLED_DIR, DROPINS_DIR);
	}

	public JadxPluginMetadata install(String locationId) {
		return installUseCase.install(locationId);
	}

	public JadxPluginMetadata resolveMetadata(String locationId) {
		JadxPluginMetadata pluginMetadata = jadx.plugins.tools.resolvers.ResolversRegistry.getResolver(locationId)
				.resolve(locationId)
				.orElseThrow(() -> new RuntimeException("Failed to resolve locationId: " + locationId));
		installUseCase.fillMetadata(pluginMetadata);
		return pluginMetadata;
	}

	public List<JadxPluginMetadata> getVersionsByLocation(String locationId, int page, int perPage) {
		List<JadxPluginMetadata> list = jadx.plugins.tools.resolvers.ResolversRegistry.getResolver(locationId)
				.resolveVersions(locationId, page, perPage);
		for (JadxPluginMetadata pluginMetadata : list) {
			installUseCase.fillMetadata(pluginMetadata);
		}
		return list;
	}

	public List<JadxPluginUpdate> updateAll() {
		return updateUseCase.updateAll();
	}

	public Optional<JadxPluginUpdate> update(String pluginId) {
		return updateUseCase.update(pluginId);
	}

	public boolean uninstall(String pluginId) {
		return uninstallUseCase.uninstall(pluginId);
	}

	public List<JadxPluginMetadata> getInstalled() {
		return listUseCase.getInstalled();
	}

	public List<JadxPluginInfo> getAllPluginsInfo() {
		return listUseCase.getAllPluginsInfo();
	}

	public List<Path> getEnabledPluginPaths() {
		return listUseCase.getEnabledPluginPaths();
	}

	public boolean changeDisabledStatus(String pluginId, boolean disabled) {
		return statusUseCase.changeDisabledStatus(pluginId, disabled);
	}
}
