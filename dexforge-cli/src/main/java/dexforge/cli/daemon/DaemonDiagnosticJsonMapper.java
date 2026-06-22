package dexforge.cli.daemon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dexforge.engine.DexForgeDiagnostic;

final class DaemonDiagnosticJsonMapper {
	private DaemonDiagnosticJsonMapper() {
	}

	static List<Map<String, Object>> toJsonDiagnostics(List<DexForgeDiagnostic> diagnostics) {
		List<Map<String, Object>> list = new ArrayList<>();
		for (DexForgeDiagnostic diagnostic : diagnostics) {
			Map<String, Object> map = new HashMap<>();
			map.put("line", diagnostic.getLine());
			map.put("character", diagnostic.getColumn());
			map.put("severity", diagnostic.getSeverity().name());
			map.put("message", diagnostic.getMessage());
			list.add(map);
		}
		return list;
	}
}
