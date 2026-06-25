package dexforge.core.service.intelligence.providers;

import dexforge.core.parser.apk.ApkLoader;
import java.util.Map;

/**
 * Interface for components that provide specific high-level insights about the project.
 */
public interface InsightProvider {
    String getName();
    Object provide(ApkLoader loader);
}
