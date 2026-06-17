package jadx.gui.device.adb;

import java.util.regex.Pattern;

public class ApkPath {
	private static final Pattern LANG_SPLIT = Pattern.compile(".*config\\.[a-z]{2,3}(-r[a-z]{2})?\\.apk$");

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
		if (LANG_SPLIT.matcher(lower).matches()) {
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
