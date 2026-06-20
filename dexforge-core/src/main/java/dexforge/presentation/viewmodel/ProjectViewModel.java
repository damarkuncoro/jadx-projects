package dexforge.presentation.viewmodel;

import dexforge.domain.model.project.Project;

/**
 * Value Object: ProjectViewModel
 * UI state model untuk menampilkan project di UI.
 * Tidak mengandung business logic.
 */
public final class ProjectViewModel {
	private final String id;
	private final String name;
	private final String status;
	private final int moduleCount;
	private final boolean hasChanges;

	private ProjectViewModel(String id, String name, String status, int moduleCount, boolean hasChanges) {
		this.id = id;
		this.name = name;
		this.status = status;
		this.moduleCount = moduleCount;
		this.hasChanges = hasChanges;
	}

	public static ProjectViewModel from(Project project) {
		return new ProjectViewModel(
				project.getProjectId().getValue(),
				project.getConfig().getName(),
				project.getStatus().name(),
				project.getModuleCount(),
				false);
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getStatus() {
		return status;
	}

	public int getModuleCount() {
		return moduleCount;
	}

	public boolean hasChanges() {
		return hasChanges;
	}
}
