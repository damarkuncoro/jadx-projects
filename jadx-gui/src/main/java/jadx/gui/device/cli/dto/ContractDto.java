package jadx.gui.device.cli.dto;

import java.util.List;

public final class ContractDto {
	private final String apiVersion;
	private final List<String> commands;

	public ContractDto(String apiVersion, List<String> commands) {
		this.apiVersion = apiVersion;
		this.commands = commands;
	}

	public String getApiVersion() {
		return apiVersion;
	}

	public List<String> getCommands() {
		return commands;
	}
}
