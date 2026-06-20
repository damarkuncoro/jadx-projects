package dexforge.engine;

import org.junit.jupiter.api.Test;

import dexforge.core.application.decompile.DecompileExitCode;
import dexforge.core.ports.decompile.DecompileProgressReporter;
import dexforge.core.ports.decompile.DecompilerEngine;
import dexforge.core.ports.decompile.DecompilerSession;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class DexForgeEngineTest {
	@Test
	public void testDecompileThroughEngineFacade() {
		DexForgeEngine engine = DexForgeEngine.using(new FakeDecompilerEngine());

		try (DexForgeSession session = engine.openSession()) {
			DexForgeDecompileResult result = session.decompile(DexForgeDecompileRequest.builder().build());

			assertThat(result.getExitCode()).isEqualTo(DecompileExitCode.SUCCESS);
			assertThat(result.isSuccess()).isTrue();
			assertThat(result.getDiagnostics()).isEmpty();
		}
	}

	@Test
	public void testClosedSessionRejectsDecompile() {
		DexForgeSession session = DexForgeEngine.using(new FakeDecompilerEngine()).openSession();

		session.close();

		assertThatThrownBy(() -> session.decompile(DexForgeDecompileRequest.builder().build()))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("closed");
	}

	private static final class FakeDecompilerEngine implements DecompilerEngine {
		@Override
		public DecompilerSession open() {
			return new FakeDecompilerSession();
		}
	}

	private static final class FakeDecompilerSession implements DecompilerSession {
		@Override
		public void load() {
		}

		@Override
		public boolean hasClasses() {
			return true;
		}

		@Override
		public boolean isSkipResources() {
			return false;
		}

		@Override
		public boolean isSkipSources() {
			return false;
		}

		@Override
		public void skipSources() {
		}

		@Override
		public int getErrorsCount() {
			return 0;
		}

		@Override
		public void printErrorsReport() {
		}

		@Override
		public void save() {
		}

		@Override
		public void save(DecompileProgressReporter progressReporter) {
		}

		@Override
		public <T> T unwrap(Class<T> type) {
			throw new IllegalArgumentException("Unsupported decompiler session type: " + type.getName());
		}

		@Override
		public void close() {
		}
	}
}
