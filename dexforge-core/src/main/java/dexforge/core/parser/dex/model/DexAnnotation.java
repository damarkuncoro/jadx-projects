package dexforge.core.parser.dex.model;

import java.util.Map;

/**
 * Represents a Java annotation in the DEX file.
 */
public final class DexAnnotation {
    private final String type;
    private final int visibility;
    private final Map<String, Object> elements;

    public DexAnnotation(String type, int visibility, Map<String, Object> elements) {
        this.type = type;
        this.visibility = visibility;
        this.elements = elements;
    }

    public String getType() { return type; }
    public int getVisibility() { return visibility; }
    public Map<String, Object> getElements() { return elements; }

    @Override
    public String toString() {
        return "@" + type + (elements.isEmpty() ? "" : elements.toString());
    }
}
