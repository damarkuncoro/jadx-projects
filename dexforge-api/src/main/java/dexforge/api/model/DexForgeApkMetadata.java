package dexforge.api.model;

import java.util.List;
import java.util.Map;

/**
 * Technical metadata for an Android APK.
 */
public final class DexForgeApkMetadata {
    private final String packageName;
    private final String versionName;
    private final long versionCode;
    private final int minSdk;
    private final int targetSdk;
    private final List<String> permissions;
    private final Map<String, String> manifestAttributes;

    public DexForgeApkMetadata(String packageName, String versionName, long versionCode,
                               int minSdk, int targetSdk, List<String> permissions,
                               Map<String, String> manifestAttributes) {
        this.packageName = packageName;
        this.versionName = versionName;
        this.versionCode = versionCode;
        this.minSdk = minSdk;
        this.targetSdk = targetSdk;
        this.permissions = java.util.Collections.unmodifiableList(permissions);
        this.manifestAttributes = java.util.Collections.unmodifiableMap(manifestAttributes);
    }

    public String getPackageName() { return packageName; }
    public String getVersionName() { return versionName; }
    public long getVersionCode() { return versionCode; }
    public int getMinSdk() { return minSdk; }
    public int getTargetSdk() { return targetSdk; }
    public List<String> getPermissions() { return permissions; }
    public Map<String, String> getManifestAttributes() { return manifestAttributes; }
}
