package dexforge.domain.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
//import java.util.stream.Collectors;

import dexforge.api.persistence.DexForgeProjectState;
import dexforge.api.persistence.DexForgeProjectStore;
import dexforge.domain.model.project.Project;
import dexforge.domain.model.project.ProjectModule;

/**
 * Orchestrates saving and loading of DDD Project aggregate using DexForgeProjectStore.
 */
public final class ProjectPersistenceService {
	private final DexForgeProjectStore store;

	public ProjectPersistenceService(DexForgeProjectStore store) {
		this.store = store;
	}

	public void saveProject(Project project, File file) throws IOException {
		DexForgeProjectState state = new DexForgeProjectState();
		state.setName(project.getConfig().getName());
		state.setDescription(project.getConfig().getDescription());
		state.setLastModified(System.currentTimeMillis());

		List<DexForgeProjectState.DexForgeModuleState> moduleStates = new ArrayList<>();
		for (ProjectModule module : project.getModules()) {
			DexForgeProjectState.DexForgeModuleState ms = new DexForgeProjectState.DexForgeModuleState();
			ms.setName(module.getName());
			ms.setType(module.getType());
			ms.setPath(module.getPath());
			ms.setSize(module.getSize());
			moduleStates.add(ms);
		}
		state.setModules(moduleStates);

		// In a real scenario, we'd also get rename history from somewhere if needed here

		store.save(state, file);
	}

	public Project loadProject(File file) throws IOException {
		DexForgeProjectState state = store.load(file);

		// Convert DTO back to DDD Aggregate
		Project project = Project.create(
				file.getAbsolutePath(),
				state.getName(),
				state.getDescription()
		);

		for (DexForgeProjectState.DexForgeModuleState ms : state.getModules()) {
			project.addModule(new ProjectModule(ms.getName(), ms.getType(), ms.getPath(), ms.getSize()));
		}

		return project;
	}
}
