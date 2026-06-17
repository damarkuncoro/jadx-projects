package jadx.gui.device.cli.dto;

public final class PackageDto {
	private final String packageName;
	private final String label;
	private final int userId;
	private final String type;
	private final String path;

	public PackageDto(String packageName, String label, int userId, String type, String path) {
		this.packageName = packageName;
		this.label = label;
		this.userId = userId;
		this.type = type;
		this.path = path;
	}

	public String getPackageName() {
		return packageName;
	}

	public String getLabel() {
		return label;
	}

	public int getUserId() {
		return userId;
	}

	public String getType() {
		return type;
	}

	public String getPath() {
		return path;
	}
}
