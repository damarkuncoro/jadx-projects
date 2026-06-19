package jadx.plugins.tools.application;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import dexforge.api.plugins.utils.CommonFileUtils;

import jadx.core.Jadx;
import jadx.core.plugins.versions.VerifyRequiredVersion;
import jadx.core.utils.StringUtils;
import jadx.core.utils.Utils;
import jadx.core.utils.exceptions.JadxRuntimeException;
import jadx.core.utils.files.FileUtils;
import jadx.plugins.tools.data.JadxInstalledPlugins;
import jadx.plugins.tools.data.JadxPluginMetadata;
import jadx.plugins.tools.domain.IPluginDownloader;
import jadx.plugins.tools.domain.IPluginMetadataLoader;
import jadx.plugins.tools.domain.IPluginRepository;
import jadx.plugins.tools.domain.IPluginUnzipper;
import jadx.plugins.tools.resolvers.IJadxPluginResolver;
import jadx.plugins.tools.resolvers.ResolversRegistry;

public class InstallPluginUseCase {
	private final IPluginRepository repository;
	private final IPluginDownloader downloader;
	private final IPluginUnzipper unzipper;
	private final IPluginMetadataLoader metadataLoader;
	private final Path installedDir;
	private final UninstallPluginUseCase uninstallUseCase;

	public InstallPluginUseCase(IPluginRepository repository, IPluginDownloader downloader,
			IPluginUnzipper unzipper, IPluginMetadataLoader metadataLoader, Path installedDir,
			UninstallPluginUseCase uninstallUseCase) {
		this.repository = repository;
		this.downloader = downloader;
		this.unzipper = unzipper;
		this.metadataLoader = metadataLoader;
		this.installedDir = installedDir;
		this.uninstallUseCase = uninstallUseCase;
	}

	public JadxPluginMetadata install(String locationId) {
		IJadxPluginResolver resolver = ResolversRegistry.getResolver(locationId);
		Supplier<List<JadxPluginMetadata>> fetchVersions;
		if (resolver.hasVersion(locationId)) {
			fetchVersions = () -> {
				JadxPluginMetadata version = resolver.resolve(locationId)
						.orElseThrow(() -> new JadxRuntimeException("Failed to resolve plugin location: " + locationId));
				return Collections.singletonList(version);
			};
		} else {
			fetchVersions = () -> resolver.resolveVersions(locationId, 1, 10);
		}
		List<JadxPluginMetadata> versionsMetadata;
		try {
			versionsMetadata = fetchVersions.get();
		} catch (Exception e) {
			throw new JadxRuntimeException("Plugin info fetch failed, locationId: " + locationId, e);
		}
		if (versionsMetadata.isEmpty()) {
			throw new JadxRuntimeException("Plugin release not found, locationId: " + locationId);
		}
		VerifyRequiredVersion verifyRequiredVersion = new VerifyRequiredVersion();
		List<String> rejectedVersions = new ArrayList<>();
		for (JadxPluginMetadata pluginMetadata : versionsMetadata) {
			fillMetadata(pluginMetadata);
			if (verifyRequiredVersion.isCompatible(pluginMetadata.getRequiredJadxVersion())) {
				install(pluginMetadata);
				return pluginMetadata;
			}
			String pluginVersion = Utils.getOrElse(pluginMetadata.getVersion(), "unknown");
			rejectedVersions.add(" version '" + pluginVersion + "' not compatible, require: "
					+ pluginMetadata.getRequiredJadxVersion());
		}
		throw new JadxRuntimeException("Can't find compatible version to install"
				+ ", current jadx version: " + verifyRequiredVersion.getJadxVersion()
				+ "\nrejected plugin versions:\n"
				+ String.join("\n", rejectedVersions));
	}

	public void install(JadxPluginMetadata metadata) {
		String reqVersionStr = metadata.getRequiredJadxVersion();
		if (!VerifyRequiredVersion.isJadxCompatible(reqVersionStr)) {
			throw new JadxRuntimeException("Can't install plugin, required version: \"" + reqVersionStr + '\"'
					+ " is not compatible with current jadx version: " + Jadx.getVersion());
		}
		uninstallUseCase.uninstall(metadata.getPluginId());

		String version = metadata.getVersion();
		String pluginBaseName = metadata.getPluginId() + (StringUtils.notBlank(version) ? '-' + version : "");
		String pluginPathStr = metadata.getPath();
		Path pluginPath = Paths.get(pluginPathStr);
		if (pluginPathStr.endsWith(".jar")) {
			Path pluginJar = installedDir.resolve(pluginBaseName + ".jar");
			copyJar(pluginPath, pluginJar);
			metadata.setPath(installedDir.relativize(pluginJar).toString());
		} else if (Files.isDirectory(pluginPath)) {
			Path pluginDir = installedDir.resolve(pluginBaseName);
			try {
				FileUtils.deleteDirIfExists(pluginDir);
				org.apache.commons.io.FileUtils.moveDirectory(pluginPath.toFile(), pluginDir.toFile());
			} catch (IOException e) {
				throw new JadxRuntimeException("Failed to install plugin: " + pluginBaseName, e);
			}
			metadata.setPath(installedDir.relativize(pluginDir).toString());
		} else {
			throw new JadxRuntimeException("Unexpected plugin path type: " + pluginPathStr);
		}
		JadxInstalledPlugins plugins = repository.load();
		plugins.getInstalled().add(metadata);
		plugins.setUpdated(System.currentTimeMillis());
		repository.save(plugins);
	}

	public void fillMetadata(JadxPluginMetadata metadata) {
		try {
			String pluginPath = metadata.getPath();
			if (downloader.needDownload(pluginPath)) {
				String ext = CommonFileUtils.getFileExtension(pluginPath);
				Path tmpJar = Files.createTempFile(metadata.getName(), "plugin." + ext);
				downloader.download(pluginPath, tmpJar);
				pluginPath = tmpJar.toAbsolutePath().toString();
			}
			if (pluginPath.endsWith(".zip")) {
				Path tmpDir = Files.createTempDirectory(metadata.getName());
				unzipper.unzip(Paths.get(pluginPath), tmpDir);
				pluginPath = tmpDir.toAbsolutePath().toString();
			}
			metadata.setPath(pluginPath);
			metadataLoader.fillMetadataFromPath(metadata, Paths.get(pluginPath));
		} catch (Exception e) {
			throw new RuntimeException("Failed to fill plugin metadata, plugin: " + metadata.getPluginId(), e);
		}
	}

	private void copyJar(Path sourceJar, Path destJar) {
		try {
			Files.copy(sourceJar, destJar, StandardCopyOption.REPLACE_EXISTING);
		} catch (Exception e) {
			throw new RuntimeException("Failed to copy plugin jar: " + sourceJar + " to: " + destJar, e);
		}
	}
}
