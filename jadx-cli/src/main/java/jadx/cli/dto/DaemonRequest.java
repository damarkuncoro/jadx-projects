package jadx.cli.dto;

import java.util.Map;

public class DaemonRequest {
	private int id;
	private String method;
	private Map<String, Object> params;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public Map<String, Object> getParams() {
		return params;
	}

	public void setParams(Map<String, Object> params) {
		this.params = params;
	}

	@Override
	public String toString() {
		return "DaemonRequest{id=" + id + ", method='" + method + "', params=" + params + "}";
	}
}
