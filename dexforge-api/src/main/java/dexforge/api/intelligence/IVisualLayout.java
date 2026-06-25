package dexforge.api.intelligence;

import java.util.Map;

/**
 * API Contract for visual layout data.
 */
public interface IVisualLayout {
    /**
     * Renders a layout node tree to a format suitable for visual display.
     */
    Map<String, Object> renderLayout(Object layoutNode);
}
