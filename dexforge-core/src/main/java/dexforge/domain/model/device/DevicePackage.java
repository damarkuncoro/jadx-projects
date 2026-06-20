package dexforge.domain.model.device;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import dexforge.domain.model.AggregateRoot;

/**
 * Aggregate Root: DevicePackage
 * Represents an APK package on a device.
 */
public class DevicePackage extends AggregateRoot {
	private final String packageName;
	private final String label;
	private final List<String> apkPaths;
	private final long size;
	private final boolean isSystemApp;

	private DevicePackage(String packageName, String label, List<String> apkPaths, long size, boolean isSystemApp) {
		super(null);
		this.packageName = Objects.requireNonNull(packageName);
		this.label = label != null ? label : packageName;
		this.apkPaths = apkPaths != null ? new ArrayList<>(apkPaths) : new ArrayList<>();
		this.size = size;
		this.isSystemApp = isSystemApp;
	}

	public static DevicePackage of(String packageName, String label, List<String> apkPaths, long size, boolean isSystemApp) {
		return new DevicePackage(packageName, label, apkPaths, size, isSystemApp);
	}

	public String getPackageName() {
		return packageName;
	}

	public String getLabel() {
		return label;
	}

	public List<String> getApkPaths() {
		return Collections.unmodifiableList(apkPaths);
	}

	public long getSize() {
		return size;
	}

	public boolean isSystemApp() {
		return isSystemApp;
	}

	public int getSplitCount() {
		return apkPaths.size();
	}
}
