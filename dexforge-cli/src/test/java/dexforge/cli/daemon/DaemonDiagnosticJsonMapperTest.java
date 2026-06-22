package dexforge.cli.daemon;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import dexforge.engine.DexForgeDiagnostic;
import dexforge.engine.DexForgeDiagnosticSeverity;

import static org.assertj.core.api.Assertions.assertThat;

class DaemonDiagnosticJsonMapperTest {
	@Test
	void testMapsDexForgeDiagnosticToLegacyJsonShape() {
		DexForgeDiagnostic diagnostic = DexForgeDiagnostic.builder(DexForgeDiagnosticSeverity.ERROR, "bad bytecode")
				.source("Example.method")
				.position(12, 4)
				.build();

		List<Map<String, Object>> mapped = DaemonDiagnosticJsonMapper.toJsonDiagnostics(List.of(diagnostic));

		assertThat(mapped).hasSize(1);
		assertThat(mapped.get(0))
				.containsEntry("line", 12)
				.containsEntry("character", 4)
				.containsEntry("severity", "ERROR")
				.containsEntry("message", "bad bytecode");
	}
}
