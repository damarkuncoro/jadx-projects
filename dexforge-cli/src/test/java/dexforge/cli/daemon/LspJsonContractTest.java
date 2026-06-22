package dexforge.cli.daemon;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import dexforge.cli.dto.DaemonResponse;
import dexforge.domain.model.project.Project;
import dexforge.engine.DexForgeClassDecompileResult;
import dexforge.engine.DexForgeClassInfo;
import dexforge.engine.DexForgeDefinitionInfo;
import dexforge.engine.DexForgeDiagnostic;
import dexforge.engine.DexForgeHover;
import dexforge.engine.DexForgeProgressReporter;
import dexforge.engine.DexForgeProjectSession;
import dexforge.engine.DexForgeSourceLocation;
import dexforge.engine.DexForgeSourcePosition;
import dexforge.engine.DexForgeSourceRange;
import dexforge.engine.DexForgeWorkspaceSymbol;

import static org.assertj.core.api.Assertions.assertThat;

class LspJsonContractTest {
	private static final String URI = "file:///sources/defpackage/HelloWorld.java";

	@Test
	void testInitializeContract() {
		DaemonResponse response = new LspService(new FakeDaemonService(null)).initialize(1);

		assertSuccessEnvelope(response, 1);
		Map<?, ?> result = (Map<?, ?>) response.getResult();
		assertThat(result.get("tool")).isEqualTo("dexforge");
		assertThat(result.get("schemaVersion")).isEqualTo(1);

		Map<?, ?> capabilities = (Map<?, ?>) result.get("capabilities");
		assertThat(capabilities.get("textDocumentSync")).isEqualTo(1);
		assertThat(capabilities.get("definitionProvider")).isEqualTo(true);
		assertThat(capabilities.get("referencesProvider")).isEqualTo(true);
		assertThat(capabilities.get("workspaceSymbolProvider")).isEqualTo(true);
		assertThat(capabilities.get("hoverProvider")).isEqualTo(true);
	}

	@Test
	void testDefinitionLocationContract() {
		LspService service = new LspService(new FakeDaemonService(new FakeProjectSession()));

		DaemonResponse response = service.definition(2, textDocumentPositionParams());

		assertSuccessEnvelope(response, 2);
		assertLocationShape((Map<?, ?>) response.getResult());
	}

	@Test
	void testReferencesLocationArrayContract() {
		LspService service = new LspService(new FakeDaemonService(new FakeProjectSession()));

		DaemonResponse response = service.references(3, textDocumentPositionParams());

		assertSuccessEnvelope(response, 3);
		List<?> result = (List<?>) response.getResult();
		assertThat(result).hasSize(1);
		assertLocationShape((Map<?, ?>) result.get(0));
	}

	@Test
	void testWorkspaceSymbolContract() {
		LspService service = new LspService(new FakeDaemonService(new FakeProjectSession()));

		DaemonResponse response = service.symbol(4, Map.of("query", "hello"));

		assertSuccessEnvelope(response, 4);
		List<?> result = (List<?>) response.getResult();
		assertThat(result).hasSize(1);

		Map<?, ?> symbol = (Map<?, ?>) result.get(0);
		assertThat(symbol.get("name")).isEqualTo("HelloWorld");
		assertThat(symbol.get("kind")).isEqualTo(5);
		assertThat(symbol.get("containerName")).isEqualTo("defpackage");
		assertLocationShape((Map<?, ?>) symbol.get("location"));
	}

	@Test
	void testHoverMarkdownContract() {
		LspService service = new LspService(new FakeDaemonService(new FakeProjectSession()));

		DaemonResponse response = service.hover(5, textDocumentPositionParams());

		assertSuccessEnvelope(response, 5);
		Map<?, ?> result = (Map<?, ?>) response.getResult();
		Map<?, ?> contents = (Map<?, ?>) result.get("contents");
		assertThat(contents.get("kind")).isEqualTo("markdown");
		assertThat(contents.get("value")).isEqualTo("```java\nclass defpackage.HelloWorld\n```");
	}

	@Test
	void testLspMethodsRequireLoadedProjectSession() {
		LspService service = new LspService(new FakeDaemonService(null));

		DaemonResponse response = service.definition(6, textDocumentPositionParams());

		assertThat(response.getId()).isEqualTo(6);
		assertThat(response.getStatus()).isEqualTo("ERROR");
		assertThat(response.getError()).isEqualTo("No active decompiler. Call 'load' first.");
	}

	private static Map<String, Object> textDocumentPositionParams() {
		return Map.of(
				"textDocument", Map.of("uri", URI),
				"position", Map.of("line", 7.0, "character", 12.0));
	}

	private static void assertSuccessEnvelope(DaemonResponse response, int id) {
		assertThat(response.getId()).isEqualTo(id);
		assertThat(response.getStatus()).isEqualTo("SUCCESS");
		assertThat(response.getResult()).isNotNull();
		assertThat(response.getError()).isNull();
	}

	private static void assertLocationShape(Map<?, ?> location) {
		assertThat(location.containsKey("uri")).isTrue();
		assertThat(location.containsKey("range")).isTrue();
		assertThat(location.get("uri")).isEqualTo(URI);

		Map<?, ?> range = (Map<?, ?>) location.get("range");
		assertThat(range.containsKey("start")).isTrue();
		assertThat(range.containsKey("end")).isTrue();
		assertPositionShape((Map<?, ?>) range.get("start"), 7, 12);
		assertPositionShape((Map<?, ?>) range.get("end"), 7, 22);
	}

	private static void assertPositionShape(Map<?, ?> position, int line, int character) {
		assertThat(position.get("line")).isEqualTo(line);
		assertThat(position.get("character")).isEqualTo(character);
	}

	private static DexForgeSourceLocation location() {
		return new DexForgeSourceLocation(
				URI,
				new DexForgeSourceRange(
						new DexForgeSourcePosition(7, 12),
						new DexForgeSourcePosition(7, 22)));
	}

	private static final class FakeDaemonService extends DaemonService {
		private final DexForgeProjectSession session;

		private FakeDaemonService(DexForgeProjectSession session) {
			this.session = session;
		}

		@Override
		public DexForgeProjectSession getProjectSession() {
			return session;
		}
	}

	private static final class FakeProjectSession implements DexForgeProjectSession {
		@Override
		public Project getProject() {
			return null;
		}

		@Override
		public int getClassesCount() {
			return 1;
		}

		@Override
		public int getResourcesCount() {
			return 0;
		}

		@Override
		public List<DexForgeClassInfo> listClasses() {
			return List.of(new DexForgeClassInfo(
					"defpackage.HelloWorld",
					"HelloWorld",
					"defpackage.HelloWorld",
					"defpackage"));
		}

		@Override
		public DexForgeClassDecompileResult decompileClass(String className) {
			throw new UnsupportedOperationException("Not needed for LSP contract tests");
		}

		@Override
		public DexForgeDefinitionInfo getDefinition(String className, int position) {
			throw new UnsupportedOperationException("Not needed for LSP contract tests");
		}

		@Override
		public DexForgeSourceLocation findDefinition(String uri, int line, int character) {
			return location();
		}

		@Override
		public List<DexForgeSourceLocation> findReferences(String uri, int line, int character) {
			return List.of(location());
		}

		@Override
		public List<DexForgeWorkspaceSymbol> findWorkspaceSymbols(String query, int limit) {
			return List.of(new DexForgeWorkspaceSymbol("HelloWorld", 5, location(), "defpackage"));
		}

		@Override
		public DexForgeHover getHover(String uri, int line, int character) {
			return new DexForgeHover("```java\nclass defpackage.HelloWorld\n```");
		}

		@Override
		public void decompileProject(java.nio.file.Path outputPath, DexForgeProgressReporter progressReporter) {
			// no-op
		}

		@Override
		public List<DexForgeDiagnostic> getDiagnostics() {
			return List.of();
		}

		@Override
		public boolean isIndexingComplete() {
			return true;
		}

		@Override
		public int getIndexedSymbolsCount() {
			return 1;
		}

		@Override
		public void close() {
			// no-op
		}
	}
}
