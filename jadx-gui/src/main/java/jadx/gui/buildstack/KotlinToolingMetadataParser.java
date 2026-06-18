package jadx.gui.buildstack;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Parser untuk file kotlin-tooling-metadata.json.
 */
public class KotlinToolingMetadataParser {
	private static final Logger LOG = LoggerFactory.getLogger(KotlinToolingMetadataParser.class);

	private KotlinToolingMetadataParser() {
	}

	/**
	 * Mem-parsing konten kotlin-tooling-metadata.json.
	 */
	public static Map<String, Object> parse(String content) {
		Map<String, Object> metadata = new LinkedHashMap<>();
		if (content == null) {
			return metadata;
		}
		try {
			JsonObject root = JsonParser.parseString(content).getAsJsonObject();
			copyJsonString(root, metadata, "buildSystem");
			copyJsonString(root, metadata, "buildSystemVersion");
			copyJsonString(root, metadata, "buildPlugin");
			copyJsonString(root, metadata, "buildPluginVersion");
			if (root.has("projectTargets") && root.get("projectTargets").isJsonArray()
					&& root.getAsJsonArray("projectTargets").size() > 0) {
				JsonObject target = root.getAsJsonArray("projectTargets").get(0).getAsJsonObject();
				copyJsonString(target, metadata, "target");
				copyJsonString(target, metadata, "platformType");
				if (target.has("extras") && target.getAsJsonObject("extras").has("android")) {
					JsonObject android = target.getAsJsonObject("extras").getAsJsonObject("android");
					copyJsonString(android, metadata, "sourceCompatibility");
					copyJsonString(android, metadata, "targetCompatibility");
				}
			}
		} catch (Exception e) {
			LOG.warn("Failed to parse Kotlin tooling metadata", e);
		}
		return metadata;
	}

	private static void copyJsonString(JsonObject root, Map<String, Object> target, String key) {
		if (root.has(key) && root.get(key).isJsonPrimitive()) {
			target.put(key, root.get(key).getAsString());
		}
	}
}
