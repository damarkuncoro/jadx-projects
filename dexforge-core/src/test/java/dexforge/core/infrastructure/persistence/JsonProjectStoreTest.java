package dexforge.core.infrastructure.persistence;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import dexforge.api.model.DexForgeNodeType;
import dexforge.api.persistence.DexForgeProjectState;
import dexforge.api.rename.DexForgeRenameAction;

import static org.assertj.core.api.Assertions.assertThat;

class JsonProjectStoreTest {

	@TempDir
	Path tempDir;

	@Test
	void testSaveAndLoad() throws IOException {
		JsonProjectStore store = new JsonProjectStore();
		File projectFile = tempDir.resolve("test.dfp").toFile();

		DexForgeProjectState state = new DexForgeProjectState();
		state.setEngineId("jadx");
		state.setInputFiles(Arrays.asList("/path/to/app.apk", "/path/to/extra.dex"));
		state.setLastModified(System.currentTimeMillis());

		DexForgeRenameAction rename1 = new DexForgeRenameAction("cls1", DexForgeNodeType.CLASS, "OldCls", "NewCls");
		state.getRenameHistory().add(rename1);

		DexForgeProjectState.DexForgeModuleState module = new DexForgeProjectState.DexForgeModuleState();
		module.setName("base.apk");
		module.setType("APK");
		module.setPath("/path/to/base.apk");
		module.setSize(1024L);
		state.getModules().add(module);

		store.save(state, projectFile);

		assertThat(projectFile).exists();

		DexForgeProjectState loadedState = store.load(projectFile);

		assertThat(loadedState.getEngineId()).isEqualTo("jadx");
		assertThat(loadedState.getInputFiles()).containsExactly("/path/to/app.apk", "/path/to/extra.dex");
		assertThat(loadedState.getRenameHistory()).hasSize(1);
		assertThat(loadedState.getRenameHistory().get(0).getNodeId()).isEqualTo("cls1");
		assertThat(loadedState.getModules()).hasSize(1);
		assertThat(loadedState.getModules().get(0).getName()).isEqualTo("base.apk");
		assertThat(loadedState.getModules().get(0).getSize()).isEqualTo(1024L);
	}

	@Test
	void loadNormalizesMissingCollectionsFromOlderProjectFiles() throws IOException {
		JsonProjectStore store = new JsonProjectStore();
		Path projectFile = tempDir.resolve("legacy.dfp");
		Files.writeString(projectFile, "{\n"
				+ "  \"name\": \"Legacy\",\n"
				+ "  \"engineId\": \"jadx\",\n"
				+ "  \"lastModified\": 123\n"
				+ "}\n");

		DexForgeProjectState loadedState = store.load(projectFile.toFile());

		assertThat(loadedState.getName()).isEqualTo("Legacy");
		assertThat(loadedState.getInputFiles()).isEmpty();
		assertThat(loadedState.getFingerprint()).isEmpty();
		assertThat(loadedState.getModules()).isEmpty();
		assertThat(loadedState.getRenameHistory()).isEmpty();
	}

	@Test
	void stateSettersDefensivelyCopyCollections() {
		DexForgeProjectState state = new DexForgeProjectState();
		state.setInputFiles(null);
		state.setFingerprint(null);
		state.setModules(null);
		state.setRenameHistory(null);

		assertThat(state.getInputFiles()).isEmpty();
		assertThat(state.getFingerprint()).isEmpty();
		assertThat(state.getModules()).isEmpty();
		assertThat(state.getRenameHistory()).isEmpty();
	}
}
