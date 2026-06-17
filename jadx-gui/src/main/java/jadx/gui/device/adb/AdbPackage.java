package jadx.gui.device.adb;

public class AdbPackage {
	private final String packageName;
	private final String path;

	public AdbPackage(String packageName, String path) {
		this.packageName = packageName;
		this.path = path;
	}

	public String getPackageName() {
		return packageName;
	}

	public String getPath() {
		return path;
	}

	public boolean isSystem() {
		return path.startsWith("/system/")
				|| path.startsWith("/product/")
				|| path.startsWith("/vendor/")
				|| path.startsWith("/apex/")
				|| path.startsWith("/system_ext/")
				|| path.startsWith("/odm/");
	}

	@Override
	public String toString() {
		return packageName + " : " + path;
	}
}
