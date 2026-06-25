package dexforge.api.analysis;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import dexforge.api.core.DexForgeProject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AnalysisApplicationServiceTest {

	@Test
	void runAllAnalysesReturnsImmutableFindingsWithDefaultQuickFixes() {
		DexForgeFinding finding = new DexForgeFinding(
				"CRYPTO_STATIC_IV",
				"Static IV found",
				DexForgeAnalysisSeverity.CRITICAL,
				null);
		AnalysisApplicationService service = new AnalysisApplicationService(List.of(analyzerReturning(List.of(finding))));

		List<DexForgeFinding> findings = service.runAllAnalyses(null);

		assertThat(findings).containsExactly(finding);
		assertThat(findings.get(0).getSuggestedFix()).contains("fresh random IV");
		assertThatThrownBy(() -> findings.add(finding))
				.isInstanceOf(UnsupportedOperationException.class);
	}

	@Test
	void runAllAnalysesSkipsNullAnalyzerResultsAndNullFindings() {
		DexForgeFinding finding = new DexForgeFinding(
				"MANIFEST_EXPORTED_COMPONENT",
				"Exported component",
				DexForgeAnalysisSeverity.LOW,
				null);
		AnalysisApplicationService service = new AnalysisApplicationService(List.of(
				analyzerReturning(null),
				analyzerReturning(Arrays.asList(null, finding))));

		List<DexForgeFinding> findings = service.runAllAnalyses(null);

		assertThat(findings).containsExactly(finding);
		assertThat(findings.get(0).getSuggestedFix()).contains("android:exported=\"false\"");
	}

	private static DexForgeAnalyzer analyzerReturning(List<DexForgeFinding> findings) {
		return new DexForgeAnalyzer() {
			@Override
			public String getName() {
				return "test";
			}

			@Override
			public String getDescription() {
				return "test analyzer";
			}

			@Override
			public List<DexForgeFinding> analyze(DexForgeProject project) {
				return findings;
			}
		};
	}
}
