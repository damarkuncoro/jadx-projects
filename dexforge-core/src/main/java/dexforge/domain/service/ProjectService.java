package dexforge.domain.service;

import java.nio.file.Path;
import java.util.List;

import dexforge.domain.model.project.Project;

/**
 * Domain Service: ProjectService
 * Handles project-level operations.
 */
public class ProjectService {

	/**
	 * Validates that the project path exists and is accessible.
	 */
	public boolean validateProjectPath(Path path) {
		return path != null && path.toFile().exists();
	}

	/**
	 * Gets all APK files from a project (base.apk + split APKs).
	 */
	public List<Path> getApkFiles(Project project) {
		return project.getModules().stream()
				.filter(m -> "apk".equals(m.getType()))
				.map(m -> Path.of(m.getPath()))
				.toList();
	}

	/**
	 * Checks if project has any modules.
	 */
	public boolean hasModules(Project project) {
		return project.getModuleCount() > 0;
	}
}
