package jadx.gui.device.protocol;

public class ApkPath {
	private final String remotePath;
	private final String localName;
	private final String type;

	public ApkPath(String remotePath) {
		this.remotePath = remotePath;
		this.localName = extractLocalName(remotePath);
		this.type = classifyType(localName);
	}

	private static String extractLocalName(String remotePath) {
		if (remotePath == null) {
			return "";
		}
		int idx = remotePath.lastIndexOf('/');
		if (idx >= 0) {
			return remotePath.substring(idx + 1);
		}
		return remotePath;
	}

	private static String classifyType(String filename) {
		String lower = filename.toLowerCase();
		if (lower.equals("base.apk")) {
			return "base";
		}
		if (lower.contains("config.arm") || lower.contains("config.x86")) {
			return "abi";
		}
		if (lower.contains("config.hdpi") || lower.contains("config.xhdpi") || lower.contains("config.xxhdpi")
				|| lower.contains("config.xxxhdpi") || lower.contains("config.mdpi") || lower.contains("config.ldpi")) {
			return "density";
		}
		if (lower.contains("config.en") || lower.contains("config.id") || lower.contains("config.es")
				|| lower.contains("config.fr") || lower.contains("config.de") || lower.contains("config.zh")
				|| lower.contains("config.ru") || lower.contains("config.it") || lower.contains("config.pt")
				|| lower.contains("config.ja") || lower.contains("config.ko")) {
			return "lang";
		}
		return "unknown";
	}

	public String getRemotePath() {
		return remotePath;
	}

	public String getLocalName() {
		return localName;
	}

	public String getType() {
		return type;
	}

	@Override
	public String toString() {
		return localName + " (" + type + ")";
	}
}
