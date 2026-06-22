package dexforge.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import dexforge.api.plugins.JadxPlugin;
import dexforge.api.plugins.loader.JadxPluginLoader;

/**
 * Bridge registry for DexForge-facing plugins while preserving the existing JADX plugin loader
 * contract.
 */
public final class DexForgePluginRegistry implements JadxPluginLoader {
	private final List<DexForgePluginLoader> dexForgeLoaders = new ArrayList<>();
	private final List<DexForgePlugin> dexForgePlugins = new ArrayList<>();
	private final List<JadxPluginLoader> loaders = new ArrayList<>();
	private final List<JadxPlugin> plugins = new ArrayList<>();

	public DexForgePluginRegistry addDexForgeLoader(DexForgePluginLoader loader) {
		dexForgeLoaders.add(Objects.requireNonNull(loader));
		return this;
	}

	public DexForgePluginRegistry addDexForgePlugin(DexForgePlugin plugin) {
		dexForgePlugins.add(Objects.requireNonNull(plugin));
		return this;
	}

	public List<DexForgePluginLoader> getDexForgeLoaders() {
		return Collections.unmodifiableList(dexForgeLoaders);
	}

	public List<DexForgePlugin> getDexForgePlugins() {
		return Collections.unmodifiableList(dexForgePlugins);
	}

	/**
	 * Compatibility bridge for existing JADX plugin loaders.
	 * Prefer {@link #addDexForgeLoader(DexForgePluginLoader)} for new code.
	 */
	@Deprecated(forRemoval = false)
	public DexForgePluginRegistry addLoader(JadxPluginLoader loader) {
		loaders.add(Objects.requireNonNull(loader));
		return this;
	}

	/**
	 * Compatibility bridge for existing JADX plugins.
	 * Prefer {@link #addDexForgePlugin(DexForgePlugin)} for new code.
	 */
	@Deprecated(forRemoval = false)
	public DexForgePluginRegistry addPlugin(JadxPlugin plugin) {
		plugins.add(Objects.requireNonNull(plugin));
		return this;
	}

	/**
	 * Compatibility bridge for existing JADX plugin loaders.
	 * Prefer {@link #getDexForgeLoaders()} for new code.
	 */
	@Deprecated(forRemoval = false)
	public List<JadxPluginLoader> getLoaders() {
		return Collections.unmodifiableList(loaders);
	}

	/**
	 * Compatibility bridge for existing JADX plugins.
	 * Prefer {@link #getDexForgePlugins()} for new code.
	 */
	@Deprecated(forRemoval = false)
	public List<JadxPlugin> getPlugins() {
		return Collections.unmodifiableList(plugins);
	}

	@Override
	public List<JadxPlugin> load() {
		List<JadxPlugin> loaded = new ArrayList<>(plugins);
		loaded.addAll(dexForgePlugins);
		for (DexForgePluginLoader loader : dexForgeLoaders) {
			loaded.addAll(loader.load());
		}
		for (JadxPluginLoader loader : loaders) {
			loaded.addAll(loader.load());
		}
		return Collections.unmodifiableList(loaded);
	}

	@Override
	public void close() {
		for (DexForgePluginLoader loader : dexForgeLoaders) {
			try {
				loader.close();
			} catch (Exception e) {
				throw new DexForgeException("PLUGIN_REGISTRY_CLOSE_FAILED", "Failed to close DexForge plugin loader", e);
			}
		}
		for (JadxPluginLoader loader : loaders) {
			try {
				loader.close();
			} catch (Exception e) {
				throw new DexForgeException("PLUGIN_REGISTRY_CLOSE_FAILED", "Failed to close plugin loader", e);
			}
		}
		dexForgeLoaders.clear();
		dexForgePlugins.clear();
		loaders.clear();
		plugins.clear();
	}
}
