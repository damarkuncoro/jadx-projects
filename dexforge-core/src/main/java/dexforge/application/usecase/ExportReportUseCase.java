package dexforge.application.usecase;

import dexforge.application.dto.ExportReportRequest;
import dexforge.application.port.ProjectRepository;
import dexforge.domain.model.project.Project;

/**
 * Use Case: ExportReportUseCase
 * Orchestrates report generation from decompiled code.
 */
@UseCase
public class ExportReportUseCase {
	private final ProjectRepository projectRepository;

	public ExportReportUseCase(ProjectRepository projectRepository) {
		this.projectRepository = projectRepository;
	}

	public void execute(ExportReportRequest request) throws ExportException {
		Project project = projectRepository.findById(request.getProjectId())
				.orElseThrow(() -> new ExportException("Project not found: " + request.getProjectId()));
		// TODO: implement report generation
	}
}
