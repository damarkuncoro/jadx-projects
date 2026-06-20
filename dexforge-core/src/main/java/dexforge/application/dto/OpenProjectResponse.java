package dexforge.application.dto;

import dexforge.domain.model.project.ProjectStatus;

/**
 * DTO: Response dari Open Project use case.
 */
public class OpenProjectResponse {
	private final String projectId;
	private final String projectName;
	private final ProjectStatus status;
	private final boolean success;
	private final String message;

	private OpenProjectResponse(String projectId, String projectName, ProjectStatus status, boolean success, String message) {
		this.projectId = projectId;
		this.projectName = projectName;
		this.status = status;
		this.success = success;
		this.message = message;
	}

	public static OpenProjectResponse success(String projectId, String projectName, ProjectStatus status) {
		return new OpenProjectResponse(projectId, projectName, status, true, "Project opened successfully");
	}

	public static OpenProjectResponse failure(String message) {
		return new OpenProjectResponse(null, null, null, false, message);
	}

	public String getProjectId() {
		return projectId;
	}

	public String getProjectName() {
		return projectName;
	}

	public ProjectStatus getStatus() {
		return status;
	}

	public boolean isSuccess() {
		return success;
	}

	public String getMessage() {
		return message;
	}

	@Override
	public String toString() {
		return "OpenProjectResponse{"
				+ "projectId='" + projectId + '\''
				+ ", projectName='" + projectName + '\''
				+ ", status=" + status
				+ ", success=" + success
				+ ", message='" + message + '\''
				+ '}';
	}
}
