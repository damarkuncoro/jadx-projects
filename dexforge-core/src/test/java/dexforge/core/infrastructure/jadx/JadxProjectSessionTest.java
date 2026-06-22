package dexforge.core.infrastructure.jadx;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import dexforge.domain.model.project.Project;
import dexforge.domain.model.project.ProjectStatus;
import dexforge.engine.DexForgeOpenProjectRequest;

import static org.assertj.core.api.Assertions.assertThat;

class JadxProjectSessionTest {

	@Test
	void testOpenAndCloseTransitionsProjectLifecycle(@TempDir Path tempDir) throws IOException {
		Path tempFile = tempDir.resolve("test.dex");
		Files.write(tempFile, new byte[] { 0 }); // create dummy file

		DexForgeOpenProjectRequest request = DexForgeOpenProjectRequest.builder(tempFile)
				.deobfuscationOn(false)
				.commentsLevel("none")
				.decompilationMode("fallback")
				.build();

		try (JadxProjectSession session = JadxProjectSession.open(request)) {
			Project project = session.getProject();
			assertThat(project).isNotNull();
			assertThat(project.getStatus()).isEqualTo(ProjectStatus.OPENED);
			assertThat(project.getProjectId().getValue()).isEqualTo(tempFile.toAbsolutePath().toString());
			assertThat(project.getModuleCount()).isEqualTo(1);
			assertThat(project.getModules().get(0).getName()).isEqualTo("test.dex");
			assertThat(project.getModules().get(0).getType()).isEqualTo("DEX");

			// Wait for indexing to complete asynchronously
			long start = System.currentTimeMillis();
			while (!session.isIndexingComplete() && (System.currentTimeMillis() - start) < 5000) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					break;
				}
			}
			assertThat(session.isIndexingComplete()).isTrue();
			assertThat(session.getIndexedSymbolsCount()).isGreaterThanOrEqualTo(0);
			assertThat(session.findWorkspaceSymbols("dummy", 10)).isEmpty();

			// Close the session inside block, but explicitly verify status beforehand
			session.close();
			assertThat(project.getStatus()).isEqualTo(ProjectStatus.CLOSED);
		}
	}
}
