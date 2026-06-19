package dexforge.cli.dto;

public class DaemonResponse {
	private int id;
	private String status;
	private Object result;
	private Object error;

	public static DaemonResponse success(int id, Object result) {
		DaemonResponse resp = new DaemonResponse();
		resp.setId(id);
		resp.setStatus("SUCCESS");
		resp.setResult(result);
		return resp;
	}

	public static DaemonResponse error(int id, Object error) {
		DaemonResponse resp = new DaemonResponse();
		resp.setId(id);
		resp.setStatus("ERROR");
		resp.setError(error);
		return resp;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}

	public Object getError() {
		return error;
	}

	public void setError(Object error) {
		this.error = error;
	}

	@Override
	public String toString() {
		return "DaemonResponse{id=" + id + ", status='" + status + "', result=" + result + ", error=" + error + "}";
	}
}
