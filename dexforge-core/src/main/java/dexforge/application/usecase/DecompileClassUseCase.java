package dexforge.application.usecase;

import dexforge.application.port.EventPublisher;
import dexforge.core.application.decompile.DecompileApplicationService;
import dexforge.domain.model.project.ProjectId;

/**
 * Use Case: DecompileClassUseCase
 * Orchestrates decompilation of a single class.
 */
@UseCase
public class DecompileClassUseCase {
	private final DecompileApplicationService decompileService;
	private final EventPublisher eventPublisher;

	public DecompileClassUseCase(DecompileApplicationService decompileService,
			EventPublisher eventPublisher) {
		this.decompileService = decompileService;
		this.eventPublisher = eventPublisher;
	}

	public Object execute(String classFullName, ProjectId projectId) throws DecompileException {
		// TODO: implement actual decompilation
		return null;
	}
}
