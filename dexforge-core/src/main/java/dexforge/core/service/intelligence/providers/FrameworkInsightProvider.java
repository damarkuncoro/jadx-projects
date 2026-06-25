package dexforge.core.service.intelligence.providers;

import dexforge.core.parser.apk.ApkLoader;
import java.util.Map;

public final class FrameworkInsightProvider implements InsightProvider {
    @Override
    public String getName() { return "framework"; }

    @Override
    public Object provide(ApkLoader loader) {
        Map<String, String> bridges = loader.getJniBridges();
        for (String key : bridges.keySet()) {
            if (key.contains("io.flutter")) return "Flutter";
            if (key.contains("com.facebook.react")) return "React Native";
            if (key.contains("com.unity3d")) return "Unity";
        }
        return "Native (Java/Kotlin)";
    }
}
