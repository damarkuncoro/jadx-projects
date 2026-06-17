package jadx.gui.device.cli.dto;

public final class ApkPathDto {
	private final String type;
	private final String remotePath;
	private final String localName;

	public ApkPathDto(String type, String remotePath, String localName) {
		this.type = type;
		this.remotePath = remotePath;
		this.localName = localName;
	}

	public String getType() {
		return type;
	}

	public String getRemotePath() {
		return remotePath;
	}

	public String getLocalName() {
		return localName;
	}
}
