package dexforge.domain.model.project;

import java.util.Objects;

import dexforge.domain.model.ValueObject;

/**
 * Value Object: Project Configuration
 * Immutable configuration untuk project.
 * Tidak memiliki identity, dua ProjectConfig sama jika atributnya sama.
 */
public final class ProjectConfig implements ValueObject {
	private final String name;
	private final String description;
	private final boolean enableSourceDebug;
	private final boolean enableDeviceExplorer;

	private ProjectConfig(String name, String description, boolean enableSourceDebug, boolean enableDeviceExplorer) {
		this.name = Objects.requireNonNull(name, "Project name cannot be null");
		this.description = description;
		this.enableSourceDebug = enableSourceDebug;
		this.enableDeviceExplorer = enableDeviceExplorer;
	}

	public static ProjectConfig create(String name, String description) {
		return new ProjectConfig(name, description, false, false);
	}

	public static ProjectConfig create(String name, String description, boolean enableSourceDebug, boolean enableDeviceExplorer) {
		return new ProjectConfig(name, description, enableSourceDebug, enableDeviceExplorer);
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public boolean isSourceDebugEnabled() {
		return enableSourceDebug;
	}

	public boolean isDeviceExplorerEnabled() {
		return enableDeviceExplorer;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ProjectConfig that = (ProjectConfig) o;
		return enableSourceDebug == that.enableSourceDebug
				&& enableDeviceExplorer == that.enableDeviceExplorer
				&& name.equals(that.name)
				&& Objects.equals(description, that.description);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, description, enableSourceDebug, enableDeviceExplorer);
	}

	@Override
	public String toString() {
		return "ProjectConfig{"
				+ "name='" + name + '\''
				+ ", description='" + description + '\''
				+ ", enableSourceDebug=" + enableSourceDebug
				+ ", enableDeviceExplorer=" + enableDeviceExplorer
				+ '}';
	}
}
