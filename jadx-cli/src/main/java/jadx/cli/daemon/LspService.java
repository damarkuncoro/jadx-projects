package jadx.cli.daemon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jadx.api.ICodeInfo;
import jadx.api.JadxDecompiler;
import jadx.api.JavaClass;
import jadx.api.JavaField;
import jadx.api.JavaMethod;
import jadx.api.JavaNode;
import jadx.cli.dto.DaemonResponse;

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
		result.put("capabilities", capabilities);
		return DaemonResponse.success(requestId, result);
	}

	@SuppressWarnings("unchecked")
	public DaemonResponse definition(int requestId, Map<String, Object> params) {
		JadxDecompiler decompiler = daemonService.getDecompiler();
		if (decompiler == null) {
			return DaemonResponse.error(requestId, "No active decompiler. Call 'load' first.");
		}
		if (params == null || !params.containsKey("textDocument") || !params.containsKey("position")) {
			return DaemonResponse.error(requestId, "Missing parameter 'textDocument' or 'position'");
		}

		Map<String, Object> doc = (Map<String, Object>) params.get("textDocument");
		String uri = (String) doc.get("uri");
		if (uri == null) {
			return DaemonResponse.error(requestId, "Missing 'uri' in textDocument");
		}

		int dotIdx = uri.lastIndexOf('.');
		if (dotIdx == -1) {
			return DaemonResponse.error(requestId, "Invalid document URI extension: " + uri);
		}
		String cleanPath = uri.substring(0, dotIdx).replace('\\', '/');
		JavaClass cls = decompiler.getClasses().stream()
				.filter(c -> cleanPath.endsWith(c.getFullName().replace('.', '/')))
				.findFirst().orElse(null);

		if (cls == null) {
			return DaemonResponse.error(requestId, "Class not found for URI: " + uri);
		}

		Map<String, Object> posObj = (Map<String, Object>) params.get("position");
		int line = ((Double) posObj.get("line")).intValue();
		int character = ((Double) posObj.get("character")).intValue();

		ICodeInfo codeInfo = cls.getCodeInfo();
		String code = codeInfo.getCodeStr();
		int offset = getPositionOffset(code, line, character);

		JavaNode node = decompiler.getJavaNodeAtPosition(codeInfo, offset);
		if (node == null) {
			node = decompiler.getClosestJavaNode(codeInfo, offset);
		}

		if (node == null) {
			return DaemonResponse.error(requestId, "No symbol found at position " + offset);
		}

		JavaClass targetCls = node.getDeclaringClass();
		if (targetCls == null && node instanceof JavaClass) {
			targetCls = (JavaClass) node;
		}

		String targetCode = targetCls != null ? targetCls.getCodeInfo().getCodeStr() : code;
		int defPos = node.getDefPos();
		Map<String, Object> startPos = getLspPosition(targetCode, defPos);

		Map<String, Object> range = new HashMap<>();
		range.put("start", startPos);
		range.put("end", startPos);

		String targetUri = uri;
		if (targetCls != null) {
			targetUri = "file:///sources/" + targetCls.getFullName().replace('.', '/') + ".java";
		}

		Map<String, Object> location = new HashMap<>();
		location.put("uri", targetUri);
		location.put("range", range);

		return DaemonResponse.success(requestId, location);
	}

	@SuppressWarnings("unchecked")
	public DaemonResponse references(int requestId, Map<String, Object> params) {
		JadxDecompiler decompiler = daemonService.getDecompiler();
		if (decompiler == null) {
			return DaemonResponse.error(requestId, "No active decompiler. Call 'load' first.");
		}
		if (params == null || !params.containsKey("textDocument") || !params.containsKey("position")) {
			return DaemonResponse.error(requestId, "Missing parameter 'textDocument' or 'position'");
		}

		Map<String, Object> doc = (Map<String, Object>) params.get("textDocument");
		String uri = (String) doc.get("uri");
		if (uri == null) {
			return DaemonResponse.error(requestId, "Missing 'uri' in textDocument");
		}

		int dotIdx = uri.lastIndexOf('.');
		if (dotIdx == -1) {
			return DaemonResponse.error(requestId, "Invalid document URI extension: " + uri);
		}
		String cleanPath = uri.substring(0, dotIdx).replace('\\', '/');
		JavaClass cls = decompiler.getClasses().stream()
				.filter(c -> cleanPath.endsWith(c.getFullName().replace('.', '/')))
				.findFirst().orElse(null);

		if (cls == null) {
			return DaemonResponse.error(requestId, "Class not found for URI: " + uri);
		}

		Map<String, Object> posObj = (Map<String, Object>) params.get("position");
		int line = ((Double) posObj.get("line")).intValue();
		int character = ((Double) posObj.get("character")).intValue();

		ICodeInfo codeInfo = cls.getCodeInfo();
		String code = codeInfo.getCodeStr();
		int offset = getPositionOffset(code, line, character);

		JavaNode node = decompiler.getJavaNodeAtPosition(codeInfo, offset);
		if (node == null) {
			node = decompiler.getClosestJavaNode(codeInfo, offset);
		}

		if (node == null) {
			return DaemonResponse.error(requestId, "No symbol found at position " + offset);
		}

		List<Map<String, Object>> locations = new ArrayList<>();
		try {
			List<JavaNode> usages = node.getUseIn();
			for (JavaNode usageNode : usages) {
				JavaClass parentCls = usageNode.getTopParentClass();
				if (parentCls == null && usageNode instanceof JavaClass) {
					parentCls = (JavaClass) usageNode;
				}
				if (parentCls == null) {
					continue;
				}
				ICodeInfo usageCodeInfo = parentCls.getCodeInfo();
				String usageCode = usageCodeInfo.getCodeStr();
				List<Integer> positions = parentCls.getUsePlacesFor(usageCodeInfo, node);
				for (int pos : positions) {
					Map<String, Object> lspPos = getLspPosition(usageCode, pos);
					Map<String, Object> range = new HashMap<>();
					range.put("start", lspPos);
					range.put("end", lspPos);

					Map<String, Object> loc = new HashMap<>();
					loc.put("uri", "file:///sources/" + parentCls.getFullName().replace('.', '/') + ".java");
					loc.put("range", range);
					locations.add(loc);
				}
			}
		} catch (Exception e) {
			return DaemonResponse.error(requestId, "Failed to resolve references: " + e.getMessage());
		}

		return DaemonResponse.success(requestId, locations);
	}

	private int getPositionOffset(String code, int line, int character) {
		int pos = 0;
		int currentLine = 0;
		while (currentLine < line && pos < code.length()) {
			int nextNewline = code.indexOf('\n', pos);
			if (nextNewline == -1) {
				break;
			}
			pos = nextNewline + 1;
			currentLine++;
		}
		return Math.min(code.length(), pos + character);
	}

	private Map<String, Object> getLspPosition(String code, int defPos) {
		int line = 0;
		int character = 0;
		int pos = 0;
		while (pos < defPos && pos < code.length()) {
			if (code.charAt(pos) == '\n') {
				line++;
				character = 0;
			} else {
				character++;
			}
			pos++;
		}
		Map<String, Object> lspPos = new HashMap<>();
		lspPos.put("line", line);
		lspPos.put("character", character);
		return lspPos;
	}

	public DaemonResponse symbol(int requestId, Map<String, Object> params) {
		JadxDecompiler decompiler = daemonService.getDecompiler();
		if (decompiler == null) {
			return DaemonResponse.error(requestId, "No active decompiler. Call 'load' first.");
		}
		String query = "";
		if (params != null && params.containsKey("query")) {
			query = ((String) params.get("query")).toLowerCase();
		}

		List<Map<String, Object>> results = new ArrayList<>();
		int count = 0;
		int limit = 100;

		for (JavaClass cls : decompiler.getClasses()) {
			if (count >= limit) {
				break;
			}
			String clsName = cls.getName();
			if (clsName.toLowerCase().contains(query)) {
				results.add(createSymbolMap(cls.getName(), 5, cls, cls.getPackage()));
				count++;
			}

			for (jadx.api.JavaMethod mth : cls.getMethods()) {
				if (count >= limit) {
					break;
				}
				if (mth.getName().toLowerCase().contains(query)) {
					results.add(createSymbolMap(mth.getName(), 6, mth, cls.getFullName()));
					count++;
				}
			}

			for (jadx.api.JavaField fld : cls.getFields()) {
				if (count >= limit) {
					break;
				}
				if (fld.getName().toLowerCase().contains(query)) {
					results.add(createSymbolMap(fld.getName(), 8, fld, cls.getFullName()));
					count++;
				}
			}
		}

		return DaemonResponse.success(requestId, results);
	}

	private Map<String, Object> createSymbolMap(String name, int kind, JavaNode node, String containerName) {
		JavaClass declClass = node.getDeclaringClass();
		if (declClass == null && node instanceof JavaClass) {
			declClass = (JavaClass) node;
		}

		String targetUri = "file:///sources/" + (declClass != null ? declClass.getFullName().replace('.', '/') : "unknown") + ".java";

		Map<String, Object> startPos = new HashMap<>();
		startPos.put("line", 0);
		startPos.put("character", 0);

		Map<String, Object> range = new HashMap<>();
		range.put("start", startPos);
		range.put("end", startPos);

		Map<String, Object> location = new HashMap<>();
		location.put("uri", targetUri);
		location.put("range", range);

		Map<String, Object> symbol = new HashMap<>();
		symbol.put("name", name);
		symbol.put("kind", kind);
		symbol.put("location", location);
		if (containerName != null) {
			symbol.put("containerName", containerName);
		}
		return symbol;
	}

	@SuppressWarnings("unchecked")
	public DaemonResponse hover(int requestId, Map<String, Object> params) {
		JadxDecompiler decompiler = daemonService.getDecompiler();
		if (decompiler == null) {
			return DaemonResponse.error(requestId, "No active decompiler. Call 'load' first.");
		}
		if (params == null || !params.containsKey("textDocument") || !params.containsKey("position")) {
			return DaemonResponse.error(requestId, "Missing parameter 'textDocument' or 'position'");
		}

		Map<String, Object> doc = (Map<String, Object>) params.get("textDocument");
		String uri = (String) doc.get("uri");
		if (uri == null) {
			return DaemonResponse.error(requestId, "Missing 'uri' in textDocument");
		}

		int dotIdx = uri.lastIndexOf('.');
		if (dotIdx == -1) {
			return DaemonResponse.error(requestId, "Invalid document URI extension: " + uri);
		}
		String cleanPath = uri.substring(0, dotIdx).replace('\\', '/');
		JavaClass cls = decompiler.getClasses().stream()
				.filter(c -> cleanPath.endsWith(c.getFullName().replace('.', '/')))
				.findFirst().orElse(null);

		if (cls == null) {
			return DaemonResponse.error(requestId, "Class not found for URI: " + uri);
		}

		Map<String, Object> posObj = (Map<String, Object>) params.get("position");
		int line = ((Double) posObj.get("line")).intValue();
		int character = ((Double) posObj.get("character")).intValue();

		ICodeInfo codeInfo = cls.getCodeInfo();
		String code = codeInfo.getCodeStr();
		int offset = getPositionOffset(code, line, character);

		JavaNode node = decompiler.getJavaNodeAtPosition(codeInfo, offset);
		if (node == null) {
			node = decompiler.getClosestJavaNode(codeInfo, offset);
		}

		if (node == null) {
			return DaemonResponse.error(requestId, "No symbol found at position " + offset);
		}

		String sig = getSymbolSignature(node);
		Map<String, Object> contents = new HashMap<>();
		contents.put("kind", "markdown");
		contents.put("value", "```java\n" + sig + "\n```");

		Map<String, Object> hoverResult = new HashMap<>();
		hoverResult.put("contents", contents);

		return DaemonResponse.success(requestId, hoverResult);
	}

	private String getSymbolSignature(JavaNode node) {
		if (node instanceof JavaClass) {
			JavaClass cls = (JavaClass) node;
			String kind = "class";
			if (cls.getAccessInfo().isInterface()) {
				kind = "interface";
			} else if (cls.getAccessInfo().isEnum()) {
				kind = "enum";
			} else if (cls.getAccessInfo().isAnnotation()) {
				kind = "@interface";
			}
			return cls.getAccessInfo().makeString(false) + kind + " " + cls.getFullName();
		}
		if (node instanceof JavaMethod) {
			JavaMethod mth = (JavaMethod) node;
			StringBuilder sb = new StringBuilder();
			sb.append(mth.getAccessFlags().makeString(false));
			if (!mth.isConstructor()) {
				sb.append(mth.getReturnType().toString()).append(" ");
			}
			sb.append(mth.getName()).append("(");
			List<jadx.core.dex.instructions.args.ArgType> args = mth.getArguments();
			for (int i = 0; i < args.size(); i++) {
				sb.append(args.get(i).toString()).append(" arg").append(i);
				if (i < args.size() - 1) {
					sb.append(", ");
				}
			}
			sb.append(")");
			return sb.toString();
		}
		if (node instanceof JavaField) {
			JavaField fld = (JavaField) node;
			return fld.getAccessFlags().makeString(false) + fld.getType().toString() + " " + fld.getName();
		}
		return node.getFullName();
	}
}
