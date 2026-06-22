package dexforge.api.plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import java.util.stream.Collectors;
import dexforge.api.exception.DexForgeException;

/**
 * Registry for DexForge plugins.
 */
public final class DexForgePluginRegistry implements DexForgePluginLoader {
	private final List<DexForgePluginLoader> loaders = new ArrayList<>();
	private final List<DexForgePlugin> plugins = new ArrayList<>();
	private final List<DexForgeExtension> extensions = new ArrayList<>();

	public DexForgePluginRegistry addLoader(DexForgePluginLoader loader) {
		loaders.add(Objects.requireNonNull(loader));
		return this;
	}

	public void registerExtension(DexForgeExtension extension) {
		extensions.add(Objects.requireNonNull(extension));
	}

	public <T extends DexForgeExtension> List<T> getExtensions(Class<T> type) {
		return extensions.stream()
				.filter(type::isInstance)
				.map(type::cast)
				.collect(Collectors.toList());
	}

	public DexForgePluginRegistry addPlugin(DexForgePlugin plugin) {
		plugins.add(Objects.requireNonNull(plugin));
		return this;
	}

	public List<DexForgePluginLoader> getLoaders() {
		return Collections.unmodifiableList(loaders);
	}

	public List<DexForgePlugin> getPlugins() {
		return Collections.unmodifiableList(plugins);
	}

	@Override
	public List<DexForgePlugin> load() {
		List<DexForgePlugin> loaded = new ArrayList<>(plugins);
		for (DexForgePluginLoader loader : loaders) {
			loaded.addAll(loader.load());
		}
		return Collections.unmodifiableList(loaded);
	}

	@Override
	public void close() {
		for (DexForgePluginLoader loader : loaders) {
			try {
				loader.close();
			} catch (Exception e) {
				throw new DexForgeException("PLUGIN_REGISTRY_CLOSE_FAILED", "Failed to close plugin loader", e);
			}
		}
		loaders.clear();
		plugins.clear();
	}

	/**
	 * bridge kept for internal use.
	 */
	@Deprecated(forRemoval = false)
	public Object unwrap() {
		return this;
	}
}
