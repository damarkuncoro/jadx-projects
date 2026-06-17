package jadx.gui.device.api;

public class AndroidPackage {
	private final String packageName;
	private final String path;

	public AndroidPackage(String packageName, String path) {
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
}
