package dexforge.core.parser.resolver;

import java.util.HashMap;
import java.util.Map;

/**
 * Hardcoded map of common Android system attribute IDs (0x0101xxxx).
 * Used when full system arsc is not available.
 */
public final class SystemResourceMap {
	private static final Map<Integer, String> SYSTEM_ATTRS = new HashMap<>();

	static {
		SYSTEM_ATTRS.put(0x01010003, "android:name");
		SYSTEM_ATTRS.put(0x01010000, "android:theme");
		SYSTEM_ATTRS.put(0x01010001, "android:label");
		SYSTEM_ATTRS.put(0x01010002, "android:icon");
		SYSTEM_ATTRS.put(0x01010010, "android:permission");
		SYSTEM_ATTRS.put(0x01010011, "android:exported");
		SYSTEM_ATTRS.put(0x01010012, "android:process");
		SYSTEM_ATTRS.put(0x01010020, "android:enabled");
		SYSTEM_ATTRS.put(0x0101020c, "android:debuggable");
		SYSTEM_ATTRS.put(0x0101021b, "android:minSdkVersion");
		SYSTEM_ATTRS.put(0x01010270, "android:targetSdkVersion");
		SYSTEM_ATTRS.put(0x01010280, "android:allowBackup");
		SYSTEM_ATTRS.put(0x0101003e, "android:id");
		SYSTEM_ATTRS.put(0x0101014f, "android:text");
		SYSTEM_ATTRS.put(0x010100d4, "android:background");
		SYSTEM_ATTRS.put(0x01010119, "android:src");
		SYSTEM_ATTRS.put(0x010100f2, "android:layout_width");
		SYSTEM_ATTRS.put(0x010100f3, "android:layout_height");
	}

	public static String get(int id) {
		return SYSTEM_ATTRS.get(id);
	}

	public static Map<Integer, String> getAll() {
		return new HashMap<>(SYSTEM_ATTRS);
	}
}
