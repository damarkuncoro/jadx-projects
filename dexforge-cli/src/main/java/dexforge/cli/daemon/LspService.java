package dexforge.cli.daemon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dexforge.cli.dto.DaemonResponse;
import dexforge.engine.DexForgeHover;
import dexforge.engine.DexForgeProjectSession;
import dexforge.engine.DexForgeSourceLocation;
import dexforge.engine.DexForgeSourcePosition;
import dexforge.engine.DexForgeSourceRange;
import dexforge.engine.DexForgeWorkspaceSymbol;

public class LspService {
	private final DaemonService daemonService;

	public LspService(DaemonService daemonService) {
		this.daemonService = daemonService;
	}

	public DaemonResponse initialize(int requestId) {
		Map<String, Object> capabilities = new HashMap<>();
		capabilities.put("textDocumentSync", 1);
		capabilities.put("definitionProvider", true);
		capabilities.put("referencesProvider", true);
		capabilities.put("workspaceSymbolProvider", true);
		capabilities.put("hoverProvider", true);

		Map<String, Object> result = new HashMap<>();
		result.put("tool", "dexforge");
		result.put("schemaVersion", 1);
		result.put("capabilities", capabilities);
		return DaemonResponse.success(requestId, result);
	}

	@SuppressWarnings("unchecked")
	public DaemonResponse definition(int requestId, Map<String, Object> params) {
		DexForgeProjectSession session = daemonService.getProjectSession();
		if (session == null) {
			return DaemonResponse.error(requestId, "No active decompiler. Call 'load' first.");
		}
		try {
			TextDocumentPosition position = parseTextDocumentPosition(params);
			return DaemonResponse.success(requestId,
					toJsonLocation(session.findDefinition(position.uri, position.line, position.character)));
		} catch (IllegalArgumentException e) {
			return DaemonResponse.error(requestId, e.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	public DaemonResponse references(int requestId, Map<String, Object> params) {
		DexForgeProjectSession session = daemonService.getProjectSession();
		if (session == null) {
			return DaemonResponse.error(requestId, "No active decompiler. Call 'load' first.");
		}
		try {
			TextDocumentPosition position = parseTextDocumentPosition(params);
			List<Map<String, Object>> locations = new ArrayList<>();
			for (DexForgeSourceLocation location : session.findReferences(position.uri, position.line, position.character)) {
				locations.add(toJsonLocation(location));
			}
			return DaemonResponse.success(requestId, locations);
		} catch (Exception e) {
			return DaemonResponse.error(requestId, "Failed to resolve references: " + e.getMessage());
		}
	}

	public DaemonResponse symbol(int requestId, Map<String, Object> params) {
		DexForgeProjectSession session = daemonService.getProjectSession();
		if (session == null) {
			return DaemonResponse.error(requestId, "No active decompiler. Call 'load' first.");
		}
		String query = "";
		if (params != null && params.containsKey("query")) {
			query = ((String) params.get("query")).toLowerCase();
		}

		List<Map<String, Object>> results = new ArrayList<>();
		for (DexForgeWorkspaceSymbol symbol : session.findWorkspaceSymbols(query, 100)) {
			results.add(toJsonSymbol(symbol));
		}

		return DaemonResponse.success(requestId, results);
	}

	@SuppressWarnings("unchecked")
	public DaemonResponse hover(int requestId, Map<String, Object> params) {
		DexForgeProjectSession session = daemonService.getProjectSession();
		if (session == null) {
			return DaemonResponse.error(requestId, "No active decompiler. Call 'load' first.");
		}
		try {
			TextDocumentPosition position = parseTextDocumentPosition(params);
			DexForgeHover hover = session.getHover(position.uri, position.line, position.character);
			Map<String, Object> contents = new HashMap<>();
			contents.put("kind", "markdown");
			contents.put("value", hover.getMarkdown());

			Map<String, Object> hoverResult = new HashMap<>();
			hoverResult.put("contents", contents);
			return DaemonResponse.success(requestId, hoverResult);
		} catch (IllegalArgumentException e) {
			return DaemonResponse.error(requestId, e.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	private TextDocumentPosition parseTextDocumentPosition(Map<String, Object> params) {
		if (params == null || !params.containsKey("textDocument") || !params.containsKey("position")) {
			throw new IllegalArgumentException("Missing parameter 'textDocument' or 'position'");
		}

		Map<String, Object> doc = (Map<String, Object>) params.get("textDocument");
		String uri = (String) doc.get("uri");
		if (uri == null) {
			throw new IllegalArgumentException("Missing 'uri' in textDocument");
		}

		Map<String, Object> posObj = (Map<String, Object>) params.get("position");
		int line = ((Double) posObj.get("line")).intValue();
		int character = ((Double) posObj.get("character")).intValue();
		return new TextDocumentPosition(uri, line, character);
	}

	private Map<String, Object> toJsonLocation(DexForgeSourceLocation location) {
		Map<String, Object> map = new HashMap<>();
		map.put("uri", location.getUri());
		map.put("range", toJsonRange(location.getRange()));
		return map;
	}

	private Map<String, Object> toJsonRange(DexForgeSourceRange range) {
		Map<String, Object> map = new HashMap<>();
		map.put("start", toJsonPosition(range.getStart()));
		map.put("end", toJsonPosition(range.getEnd()));
		return map;
	}

	private Map<String, Object> toJsonPosition(DexForgeSourcePosition position) {
		Map<String, Object> map = new HashMap<>();
		map.put("line", position.getLine());
		map.put("character", position.getCharacter());
		return map;
	}

	private Map<String, Object> toJsonSymbol(DexForgeWorkspaceSymbol symbol) {
		Map<String, Object> map = new HashMap<>();
		map.put("name", symbol.getName());
		map.put("kind", symbol.getKind());
		map.put("location", toJsonLocation(symbol.getLocation()));
		if (symbol.getContainerName() != null) {
			map.put("containerName", symbol.getContainerName());
		}
		return map;
	}

	private static final class TextDocumentPosition {
		private final String uri;
		private final int line;
		private final int character;

		private TextDocumentPosition(String uri, int line, int character) {
			this.uri = uri;
			this.line = line;
			this.character = character;
		}
	}
}
