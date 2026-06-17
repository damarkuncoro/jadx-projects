package jadx.gui.device.cli.dto;

import java.util.List;
import java.util.Map;

public final class PullResultDto {
	private final String packageName;
	private final String deviceSerial;
	private final int androidUser;
	private final String workspace;
	private final List<String> apks;
	private final List<ApkPathDto> apkPaths;
	private final Map<String, String> reports;

	private String decompiledDir;
	private String decompiledPath;
	private Long durationMs;

	public PullResultDto(String packageName, String deviceSerial, int androidUser, String workspace,
			List<String> apks, List<ApkPathDto> apkPaths, Map<String, String> reports) {
		this.packageName = packageName;
		this.deviceSerial = deviceSerial;
		this.androidUser = androidUser;
		this.workspace = workspace;
		this.apks = apks;
		this.apkPaths = apkPaths;
		this.reports = reports;
	}

	public String getPackageName() {
		return packageName;
	}

	public String getDeviceSerial() {
		return deviceSerial;
	}

	public int getAndroidUser() {
		return androidUser;
	}

	public String getWorkspace() {
		return workspace;
	}

	public List<String> getApks() {
		return apks;
	}

	public List<ApkPathDto> getApkPaths() {
		return apkPaths;
	}

	public Map<String, String> getReports() {
		return reports;
	}

	public String getDecompiledDir() {
		return decompiledDir;
	}

	public void setDecompiledDir(String decompiledDir) {
		this.decompiledDir = decompiledDir;
	}

	public String getDecompiledPath() {
		return decompiledPath;
	}

	public void setDecompiledPath(String decompiledPath) {
		this.decompiledPath = decompiledPath;
	}

	public Long getDurationMs() {
		return durationMs;
	}

	public void setDurationMs(Long durationMs) {
		this.durationMs = durationMs;
	}
}
