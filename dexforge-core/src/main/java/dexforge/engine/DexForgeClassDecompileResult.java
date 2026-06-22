package dexforge.engine;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class DexForgeClassDecompileResult {
	private final String code;
	private final Map<Integer, Integer> lineMapping;
	private final List<DexForgeDiagnostic> diagnostics;

	public DexForgeClassDecompileResult(
			String code,
			Map<Integer, Integer> lineMapping,
			List<DexForgeDiagnostic> diagnostics) {
		this.code = Objects.requireNonNull(code, "Code cannot be null");
		this.lineMapping = Map.copyOf(Objects.requireNonNull(lineMapping, "Line mapping cannot be null"));
		this.diagnostics = List.copyOf(Objects.requireNonNull(diagnostics, "Diagnostics cannot be null"));
	}

	public String getCode() {
		return code;
	}

	public Map<Integer, Integer> getLineMapping() {
		return lineMapping;
	}

	public List<DexForgeDiagnostic> getDiagnostics() {
		return diagnostics;
	}
}
