package dexforge.core.analysis;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import dexforge.engine.DexForgeDiagnostic;
import dexforge.engine.DexForgeDiagnosticCategory;

/**
 * Analyzer for decompilation errors to provide recommendations.
 */
public class ErrorDensityAnalyzer {

	/**
	 * Analyzes error density in a project and returns recommendations.
	 */
	public static AnalysisResult analyze(List<DexForgeDiagnostic> diagnostics, int totalClasses) {
		Map<DexForgeDiagnosticCategory, Long> categoryCounts = diagnostics.stream()
				.filter(d -> isDecompilerError(d))
				.map(ErrorDensityAnalyzer::getCategorySafe)
				.filter(cat -> cat != null)
				.collect(Collectors.groupingBy(cat -> cat, Collectors.counting()));

		long totalErrors = diagnostics.stream()
				.filter(d -> isDecompilerError(d))
				.count();
		double errorDensity = (double) totalErrors / Math.max(1, totalClasses);

		return new AnalysisResult(errorDensity, categoryCounts, totalErrors);
	}

	private static boolean isDecompilerError(DexForgeDiagnostic diag) {
		return diag.getMethod() != null;
	}

	private static DexForgeDiagnosticCategory getCategorySafe(DexForgeDiagnostic diag) {
		String msg = diag.getMessage();
		if (msg == null) {
			return null;
		}
		if (msg.contains("Regions count limit reached")) {
			return DexForgeDiagnosticCategory.OVERFLOW_REGION;
		}
		if (msg.contains("Code restructure failed")) {
			return DexForgeDiagnosticCategory.CODE_RESTRUCTURE_FAILED;
		}
		if (msg.contains("Type inference failed")) {
			return DexForgeDiagnosticCategory.TYPE_INFERENCE_FAILED;
		}
		return null;
	}

	public static final class AnalysisResult {
		private final double errorDensity;
		private final Map<DexForgeDiagnosticCategory, Long> categoryCounts;
		private final long totalErrors;

		AnalysisResult(double errorDensity, Map<DexForgeDiagnosticCategory, Long> categoryCounts, long totalErrors) {
			this.errorDensity = errorDensity;
			this.categoryCounts = categoryCounts;
			this.totalErrors = totalErrors;
		}

		public double getErrorDensity() {
			return errorDensity;
		}

		public Map<DexForgeDiagnosticCategory, Long> getCategoryCounts() {
			return categoryCounts;
		}

		public long getTotalErrors() {
			return totalErrors;
		}

		/**
		 * Returns true if error density is high enough to warrant settings adjustment.
		 */
		public boolean isHighErrorDensity() {
			return errorDensity > 0.1; // More than 10% error rate
		}

		/**
		 * Returns recommended type updates limit based on error density.
		 */
		public int getRecommendedTypeUpdatesLimit() {
			if (errorDensity > 0.3) {
				return 50000; // High error density - increase limit significantly
			}
			if (errorDensity > 0.1) {
				return 25000; // Medium error density
			}
			return 10000; // Default
		}
	}
}