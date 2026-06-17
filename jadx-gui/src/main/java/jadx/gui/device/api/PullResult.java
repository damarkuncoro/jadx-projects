package jadx.gui.device.api;

import java.util.List;

public class PullResult {
	private final List<ApkPath> paths;

	public PullResult(List<ApkPath> paths) {
		this.paths = paths;
	}

	public List<ApkPath> getPaths() {
		return paths;
	}
}
