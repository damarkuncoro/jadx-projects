package dexforge.engine;

import java.util.Collections;
import java.util.List;

import dexforge.core.application.decompile.DecompileResult;

/**
 * Public DexForge decompile result.
 */
public final class DexForgeDecompileResult {
	private final int exitCode;
	private final int errorsCount;
	private final List<DexForgeDiagnostic> diagnostics;

	private DexForgeDecompileResult(int exitCode, int errorsCount, List<DexForgeDiagnostic> diagnostics) {
		this.exitCode = exitCode;
		this.errorsCount = errorsCount;
		this.diagnostics = List.copyOf(diagnostics);
	}

	static DexForgeDecompileResult from(DecompileResult result) {
		return new DexForgeDecompileResult(result.getExitCode(), result.getErrorsCount(), Collections.emptyList());
	}

	public int getExitCode() {
		return exitCode;
	}

	public int getErrorsCount() {
		return errorsCount;
	}

	public boolean isSuccess() {
		return exitCode == 0;
	}

	public List<DexForgeDiagnostic> getDiagnostics() {
		return diagnostics;
	}
}
