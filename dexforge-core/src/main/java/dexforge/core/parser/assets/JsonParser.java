package dexforge.core.parser.assets;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Basic JSON parser for assets analysis.
 * Focused on extracting API endpoints and configuration keys.
 */
public final class JsonParser {

    public Map<String, String> extractPairs(String json) {
        Map<String, String> pairs = new HashMap<>();
        // Simple regex to find "key": "value" patterns
        Pattern pattern = Pattern.compile("\"([^\"]+)\"\\s*:\\s*\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(json);

        while (matcher.find()) {
            pairs.put(matcher.group(1), matcher.group(2));
        }

        return pairs;
    }

    public boolean isPotentialApiEndpoint(String value) {
        return value != null && (value.startsWith("http://") || value.startsWith("https://") ||
               value.contains(".com/") || value.contains(".io/"));
    }
}
