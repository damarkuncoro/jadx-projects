package dexforge.application.dto;

import dexforge.domain.model.project.ProjectId;

/**
 * DTO: Request untuk Export Report use case.
 */
public class ExportReportRequest {
	private final ProjectId projectId;
	private final String outputPath;
	private final String format;

	public ExportReportRequest(ProjectId projectId, String outputPath, String format) {
		this.projectId = projectId;
		this.outputPath = outputPath;
		this.format = format;
	}

	public ProjectId getProjectId() {
		return projectId;
	}

	public String getOutputPath() {
		return outputPath;
	}

	public String getFormat() {
		return format;
	}
}
