package dexforge.api.intelligence;

import java.util.Map;

/**
 * API Contract for high-level project insights.
 * Decouples the GUI from specific core implementations.
 */
public interface IProjectIntelligence {
    /**
     * Gets a summary of insights about the loaded APK.
     * Includes framework, packer, security scoring, etc.
     */
    Map<String, Object> getProjectInsights();
}
