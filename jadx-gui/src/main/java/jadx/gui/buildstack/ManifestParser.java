package jadx.gui.buildstack;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser untuk file AndroidManifest.xml.
 */
public class ManifestParser {

	private ManifestParser() {
	}

	/**
	 * Mem-parsing konten AndroidManifest.xml.
	 */
	public static Map<String, String> parse(String content) {
		Map<String, String> manifest = new LinkedHashMap<>();
		if (content == null) {
			return manifest;
		}
		putXmlAttr(content, manifest, "package", "package");
		putXmlAttr(content, manifest, "versionName", "android:versionName");
		putXmlAttr(content, manifest, "versionCode", "android:versionCode");
		putXmlAttr(content, manifest, "compileSdkVersion", "android:compileSdkVersion");
		putXmlAttr(content, manifest, "platformBuildVersionName", "platformBuildVersionName");
		putXmlAttr(content, manifest, "minSdkVersion", "android:minSdkVersion");
		putXmlAttr(content, manifest, "targetSdkVersion", "android:targetSdkVersion");
		putApplicationName(content, manifest);
		return manifest;
	}

	private static void putXmlAttr(String content, Map<String, String> target, String key, String attrName) {
		Pattern pattern = Pattern.compile(Pattern.quote(attrName) + "=\"([^\"]+)\"");
		Matcher matcher = pattern.matcher(content);
		if (matcher.find()) {
			target.put(key, matcher.group(1));
		}
	}

	private static void putApplicationName(String content, Map<String, String> manifest) {
		Pattern pattern = Pattern.compile("<application\\b[^>]*\\bandroid:name=\"([^\"]+)\"", Pattern.DOTALL);
		Matcher matcher = pattern.matcher(content);
		if (matcher.find()) {
			manifest.put("applicationName", matcher.group(1));
		}
	}
}
