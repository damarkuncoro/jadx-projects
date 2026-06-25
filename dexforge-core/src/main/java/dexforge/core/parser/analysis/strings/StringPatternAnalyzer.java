package dexforge.core.parser.analysis.strings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import dexforge.core.parser.dex.service.DexFastIndexer;

/**
 * Analyzes string pools for interesting patterns like URLs, IPs, API keys.
 */
public final class StringPatternAnalyzer {
    private final DexFastIndexer indexer;

    private static final Map<String, Pattern> PATTERNS = new HashMap<>();
    static {
        PATTERNS.put("URL", Pattern.compile("https?://[\\w.-]+(?:\\.[\\w.-]+)+[/\\w\\._\\-\\?&=%]*"));
        PATTERNS.put("IPv4", Pattern.compile("\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b"));
        PATTERNS.put("AWS_KEY", Pattern.compile("AKIA[0-9A-Z]{16}"));
        PATTERNS.put("GOOGLE_API_KEY", Pattern.compile("AIza[0-9A-Za-z-_]{35}"));
        PATTERNS.put("EMAIL", Pattern.compile("[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,6}"));
        PATTERNS.put("FIREBASE_URL", Pattern.compile("https://[\\w-]+\\.firebaseio\\.com"));
        PATTERNS.put("JWT_TOKEN", Pattern.compile("ey[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_=]+\\.?[A-Za-z0-9-_.+/=]*"));
    }

    public StringPatternAnalyzer(DexFastIndexer indexer) {
        this.indexer = indexer;
    }

    public Map<String, List<String>> analyze() {
        Map<String, List<String>> results = new HashMap<>();
        for (String label : PATTERNS.keySet()) {
            results.put(label, new ArrayList<>());
        }

        // Iterate through all strings in the DEX
        for (int i = 0; i < indexer.getStringCount(); i++) {
            String s = indexer.getStringPool().getString(i);
            if (s == null) continue;

            for (Map.Entry<String, Pattern> entry : PATTERNS.entrySet()) {
                if (entry.getValue().matcher(s).find()) {
                    results.get(entry.getKey()).add(s);
                }
            }
        }

        return results;
    }
}
