package dexforge.core.model.common.ui.model;

/**
 * REUSEABLE model for a UI component property (e.g., text, color, visibility).
 */
public final class UiProperty {
    private final String name;
    private final String value;
    private final String resolvedValue;

    public UiProperty(String name, String value, String resolvedValue) {
        this.name = name;
        this.value = value;
        this.resolvedValue = resolvedValue;
    }

    public String getName() { return name; }
    public String getValue() { return value; }
    public String getResolvedValue() { return resolvedValue; }
}
