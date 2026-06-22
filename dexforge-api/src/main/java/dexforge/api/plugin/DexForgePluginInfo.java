package dexforge.api.plugin;

import java.util.Objects;

/**
 * Metadata about a DexForge plugin.
 */
public final class DexForgePluginInfo {
	private final String id;
	private final String name;
	private final String description;
	private final String version;
	private final String author;

	public DexForgePluginInfo(String id, String name, String description, String version, String author) {
		this.id = Objects.requireNonNull(id);
		this.name = Objects.requireNonNull(name);
		this.description = description;
		this.version = version;
		this.author = author;
	}

	public String getId() { return id; }
	public String getName() { return name; }
	public String getDescription() { return description; }
	public String getVersion() { return version; }
	public String getAuthor() { return author; }

	@Override
	public String toString() {
		return String.format("%s v%s (%s)", name, version, id);
	}
}
