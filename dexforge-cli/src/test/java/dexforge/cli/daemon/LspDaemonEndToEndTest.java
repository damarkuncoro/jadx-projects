package dexforge.cli.daemon;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import dexforge.cli.dto.DaemonRequest;
import dexforge.cli.dto.DaemonResponse;
import dexforge.engine.DexForgeProjectSession;

import static org.assertj.core.api.Assertions.assertThat;

class LspDaemonEndToEndTest {

	@Test
	void testDaemonLifecycleAndLspFlow(@TempDir Path tempDir) throws IOException {
		Path tempFile = tempDir.resolve("test.dex");
		Files.write(tempFile, new byte[] { 0 }); // dummy file

		DaemonCommandRouter router = new DaemonCommandRouter();

		// 1. Initialize
		DaemonRequest initRequest = new DaemonRequest();
		initRequest.setId(1);
		initRequest.setMethod("initialize");
		DaemonResponse initResponse = router.route(initRequest);

		assertThat(initResponse.getId()).isEqualTo(1);
		assertThat(initResponse.getStatus()).isEqualTo("SUCCESS");
		Map<?, ?> initResult = (Map<?, ?>) initResponse.getResult();
		assertThat(initResult.get("tool")).isEqualTo("dexforge");
		assertThat(initResult.get("schemaVersion")).isEqualTo(1);

		// 2. Load project
		DaemonRequest loadRequest = new DaemonRequest();
		loadRequest.setId(2);
		loadRequest.setMethod("load");
		loadRequest.setParams(Map.of(
				"path", tempFile.toAbsolutePath().toString(),
				"deobfuscationOn", false,
				"commentsLevel", "none",
				"decompilationMode", "fallback"));
		DaemonResponse loadResponse = router.route(loadRequest);

		assertThat(loadResponse.getId()).isEqualTo(2);
		assertThat(loadResponse.getStatus()).isEqualTo("SUCCESS");
		Map<?, ?> loadResult = (Map<?, ?>) loadResponse.getResult();
		assertThat(loadResult.get("classesCount")).isNotNull();

		// 3. Await background indexing completion
		DexForgeProjectSession session = router.getDaemonService().getProjectSession();
		assertThat(session).isNotNull();

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

		// 4. Test workspace symbol query
		DaemonRequest symbolRequest = new DaemonRequest();
		symbolRequest.setId(3);
		symbolRequest.setMethod("workspace/symbol");
		symbolRequest.setParams(Map.of("query", "dummy"));
		DaemonResponse symbolResponse = router.route(symbolRequest);

		assertThat(symbolResponse.getId()).isEqualTo(3);
		assertThat(symbolResponse.getStatus()).isEqualTo("SUCCESS");
		List<?> symbols = (List<?>) symbolResponse.getResult();
		assertThat(symbols).isEmpty(); // No symbols in dummy file

		// 5. Exit daemon
		DaemonRequest exitRequest = new DaemonRequest();
		exitRequest.setId(4);
		exitRequest.setMethod("exit");
		DaemonResponse exitResponse = router.route(exitRequest);

		assertThat(exitResponse.getId()).isEqualTo(4);
		assertThat(exitResponse.getStatus()).isEqualTo("SUCCESS");

		router.getDaemonService().close();
	}
}
