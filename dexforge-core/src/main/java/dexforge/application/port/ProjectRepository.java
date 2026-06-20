package dexforge.application.port;

import java.util.List;
import java.util.Optional;

import dexforge.domain.model.project.Project;
import dexforge.domain.model.project.ProjectId;

/**
 * Port (Output adapter): Repository untuk Project aggregate.
 * Infrastructure layer harus implement interface ini.
 *
 * Separation of concerns: Application layer hanya depend pada port, tidak pada concrete repository.
 */
public interface ProjectRepository {
	/**
	 * Save project (create atau update).
	 */
	void save(Project project);

	/**
	 * Find project by ID.
	 */
	Optional<Project> findById(ProjectId id);

	/**
	 * Find all projects.
	 */
	List<Project> findAll();

	/**
	 * Delete project by ID.
	 */
	void deleteById(ProjectId id);

	/**
	 * Check apakah project dengan ID tertentu exist.
	 */
	boolean existsById(ProjectId id);
}
