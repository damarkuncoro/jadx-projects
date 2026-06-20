package dexforge.application.dto;

/**
 * DTO: Request untuk Open Project use case.
 * Data transfer object antara presentation layer dan application layer.
 * Immutable dan hanya berisi data yang di-transfer.
 */
public class OpenProjectRequest {
	private final String projectPath;
	private final String projectName;
	private final String description;

	public OpenProjectRequest(String projectPath, String projectName, String description) {
		this.projectPath = projectPath;
		this.projectName = projectName;
		this.description = description;
	}

	public String getProjectPath() {
		return projectPath;
	}

	public String getProjectName() {
		return projectName;
	}

	public String getDescription() {
		return description;
	}

	@Override
	public String toString() {
		return "OpenProjectRequest{"
				+ "projectPath='" + projectPath + '\''
				+ ", projectName='" + projectName + '\''
				+ ", description='" + description + '\''
				+ '}';
	}
}
