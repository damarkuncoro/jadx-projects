package jadx.gui.device.cli.dto;

import java.util.Map;

public final class RequestDto {
	private int id;
	private String method;
	private Map<String, Object> params;

	public RequestDto() {
	}

	public RequestDto(int id, String method, Map<String, Object> params) {
		this.id = id;
		this.method = method;
		this.params = params;
	}

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
}
