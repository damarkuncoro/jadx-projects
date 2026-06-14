package jadx.gui.device.protocol;

public class AdbPackage {
	private final String packageName;
	private final String path;
	private final boolean system;

	public AdbPackage(String packageName, String path) {
		this.packageName = packageName;
		this.path = path;
		this.system = checkIsSystem(path);
	}

	private static boolean checkIsSystem(String path) {
		if (path == null) {
			return false;
		}
		return path.startsWith("/system/")
				|| path.startsWith("/system_ext/")
				|| path.startsWith("/vendor/")
				|| path.startsWith("/product/")
				|| path.startsWith("/apex/");
	}

	public String getPackageName() {
		return packageName;
	}

	public String getPath() {
		return path;
	}

	public boolean isSystem() {
		return system;
	}

	@Override
	public String toString() {
		return packageName + " (" + (system ? "system" : "user") + ")";
	}
}
