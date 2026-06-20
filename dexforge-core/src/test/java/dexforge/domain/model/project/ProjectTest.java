package dexforge.domain.model.project;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ProjectTest {

	@Test
	public void testCreateProject() {
		ProjectId id = ProjectId.of("/path/to/project");
		ProjectConfig config = ProjectConfig.create("Test", "test project");
		Project project = Project.create(id, config, java.nio.file.Paths.get("/path/to/project"));

		assertNotNull(project);
		assertEquals("Test", project.getConfig().getName());
		assertEquals(ProjectStatus.CREATED, project.getStatus());
	}

	@Test
	public void testOpenProject() {
		ProjectId id = ProjectId.of("/path/to/project");
		ProjectConfig config = ProjectConfig.create("Test", "test");
		Project project = Project.create(id, config, java.nio.file.Paths.get("/path/to/project"));

		project.open();
		assertEquals(ProjectStatus.OPENED, project.getStatus());
	}

	@Test
	public void testOpenTwiceThrows() {
		ProjectId id = ProjectId.of("/path/to/project");
		ProjectConfig config = ProjectConfig.create("Test", "test");
		Project project = Project.create(id, config, java.nio.file.Paths.get("/path/to/project"));

		project.open();
		assertThrows(IllegalStateException.class, project::open);
	}
}
