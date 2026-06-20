package dexforge.infrastructure.adapter.jadx;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import dexforge.application.port.ProjectRepository;
import dexforge.domain.model.project.Project;
import dexforge.domain.model.project.ProjectId;

/**
 * Infrastructure Adapter: JadxProjectRepositoryAdapter
 * Implements ProjectRepository port using simple in-memory storage.
 */
public class JadxProjectRepositoryAdapter implements ProjectRepository {
	private final ThreadLocal<Project> currentProject = new ThreadLocal<>();

	@Override
	public void save(Project project) {
		currentProject.set(project);
	}

	@Override
	public Optional<Project> findById(ProjectId id) {
		return Optional.ofNullable(currentProject.get())
				.filter(p -> p.getProjectId().equals(id));
	}

	@Override
	public List<Project> findAll() {
		Project project = currentProject.get();
		if (project == null) {
			return Collections.emptyList();
		}
		return List.of(project);
	}

	@Override
	public void deleteById(ProjectId id) {
		currentProject.remove();
	}

	@Override
	public boolean existsById(ProjectId id) {
		return currentProject.get() != null && currentProject.get().getProjectId().equals(id);
	}
}
