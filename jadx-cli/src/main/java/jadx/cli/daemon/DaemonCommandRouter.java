package jadx.cli.daemon;

import java.util.Map;

import jadx.cli.dto.DaemonRequest;
import jadx.cli.dto.DaemonResponse;

public class DaemonCommandRouter {
	private final DaemonService daemonService;
	private final LspService lspService;

	public DaemonCommandRouter() {
		this.daemonService = new DaemonService();
		this.lspService = new LspService(daemonService);
	}

	public DaemonService getDaemonService() {
		return daemonService;
	}

	public DaemonResponse route(DaemonRequest request) {
		String method = request.getMethod();
		Map<String, Object> params = request.getParams();
		int id = request.getId();

		switch (method) {
			case "initialize":
				return lspService.initialize(id);

			case "textDocument/definition":
				return lspService.definition(id, params);

			case "textDocument/references":
				return lspService.references(id, params);

			case "textDocument/hover":
				return lspService.hover(id, params);

			case "workspace/symbol":
				return lspService.symbol(id, params);

			case "load":
				if (params == null || !params.containsKey("path")) {
					return DaemonResponse.error(id, "Missing parameter 'path'");
				}
				return daemonService.load(id, (String) params.get("path"), params);

			case "list-classes":
				return daemonService.listClasses(id);

			case "decompile":
				if (params == null || !params.containsKey("className")) {
					return DaemonResponse.error(id, "Missing parameter 'className'");
				}
				return daemonService.decompile(id, (String) params.get("className"));

			case "get-definition":
				if (params == null || !params.containsKey("className") || !params.containsKey("pos")) {
					return DaemonResponse.error(id, "Missing parameter 'className' or 'pos'");
				}
				String defClass = (String) params.get("className");
				int pos = ((Double) params.get("pos")).intValue();
				return daemonService.getDefinition(id, defClass, pos);

			case "exit":
				return DaemonResponse.success(id, "Exiting");

			default:
				return DaemonResponse.error(id, "Unknown method: " + method);
		}
	}
}
