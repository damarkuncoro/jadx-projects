package jadx.gui.device.cli.dto;

import java.util.Map;

public final class ErrorDto {
	private final String status;
	private final String command;
	private final String code;
	private final String message;
	private final Map<String, Object> details;

	public ErrorDto(String status, String command, String code, String message, Map<String, Object> details) {
		this.status = status;
		this.command = command;
		this.code = code;
		this.message = message;
		this.details = details;
	}

	public String getStatus() {
		return status;
	}

	public String getCommand() {
		return command;
	}

	public String getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}

	public Map<String, Object> getDetails() {
		return details;
	}
}
