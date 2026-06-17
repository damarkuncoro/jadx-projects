package jadx.gui.device.cli.dto;

public final class ResponseDto {
	private int id;
	private String status;
	private Object result;
	private ErrorDto error;

	public ResponseDto() {
	}

	public ResponseDto(int id, String status, Object result, ErrorDto error) {
		this.id = id;
		this.status = status;
		this.result = result;
		this.error = error;
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

	public ErrorDto getError() {
		return error;
	}

	public void setError(ErrorDto error) {
		this.error = error;
	}
}
