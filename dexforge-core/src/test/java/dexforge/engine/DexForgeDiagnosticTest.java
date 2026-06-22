package dexforge.engine;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DexForgeDiagnosticTest {

	@Test
	void toJson_minimalDiagnostic() {
		DexForgeDiagnostic diag = DexForgeDiagnostic.builder(DexForgeDiagnosticSeverity.ERROR, "test error").build();
		String json = diag.toJson();

		assertThat(json).contains("\"severity\":\"error\"");
		assertThat(json).contains("\"message\":\"test error\"");
		assertThat(json).doesNotContain("\"source\"");
		assertThat(json).doesNotContain("\"method\"");
	}

	@Test
	void toJson_fullDiagnostic() {
		DexForgeDiagnostic diag = DexForgeDiagnostic.builder(DexForgeDiagnosticSeverity.WARNING, "method error")
				.source("com.example.TestClass")
				.method("testMethod")
				.position(10, 5)
				.build();
		String json = diag.toJson();

		assertThat(json).contains("\"severity\":\"warning\"");
		assertThat(json).contains("\"message\":\"method error\"");
		assertThat(json).contains("\"source\":\"com.example.TestClass\"");
		assertThat(json).contains("\"method\":\"testMethod\"");
		assertThat(json).contains("\"line\":10");
		assertThat(json).contains("\"column\":5");
	}

	@Test
	void toJson_nullMethodOmitted() {
		DexForgeDiagnostic diag = DexForgeDiagnostic.builder(DexForgeDiagnosticSeverity.INFO, "info message")
				.source("some/source")
				.build();
		String json = diag.toJson();

		assertThat(json).contains("\"severity\":\"info\"");
		assertThat(json).contains("\"source\":\"some/source\"");
		assertThat(json).doesNotContain("\"method\"");
	}
}