package dexforge.domain.model.project;

import java.util.Objects;

/**
 * Entity: ProjectModule
 * Represents satu module/library dalam project.
 * Contoh: base.apk, modules.apk, library.jar
 */
public class ProjectModule {
	private final String name;
	private final String type;
	private final String path;
	private final long size;

	public ProjectModule(String name, String type, String path, long size) {
		this.name = Objects.requireNonNull(name, "Module name cannot be null");
		this.type = Objects.requireNonNull(type, "Module type cannot be null");
		this.path = Objects.requireNonNull(path, "Module path cannot be null");
		this.size = size;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public String getPath() {
		return path;
	}

	public long getSize() {
		return size;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ProjectModule that = (ProjectModule) o;
		return name.equals(that.name)
				&& type.equals(that.type);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, type);
	}

	@Override
	public String toString() {
		return "ProjectModule{"
				+ "name='" + name + '\''
				+ ", type='" + type + '\''
				+ ", path='" + path + '\''
				+ ", size=" + size
				+ '}';
	}
}
