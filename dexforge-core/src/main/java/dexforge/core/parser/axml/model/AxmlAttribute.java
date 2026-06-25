package dexforge.core.parser.axml.model;

import dexforge.core.parser.arsc.model.ArscResourceValue;

/**
 * Enhanced model for AXML attributes to support accurate decompilation.
 */
public final class AxmlAttribute {
    private final String name;
    private final String rawValue;
    private final int nameResourceId;
    private final ArscResourceValue typedValue;

    public AxmlAttribute(String name, String rawValue, int nameResourceId, ArscResourceValue typedValue) {
        this.name = name;
        this.rawValue = rawValue;
        this.nameResourceId = nameResourceId;
        this.typedValue = typedValue;
    }

    public String getName() { return name; }
    public String getRawValue() { return rawValue; }
    public int getNameResourceId() { return nameResourceId; }
    public ArscResourceValue getTypedValue() { return typedValue; }

    public String getValue() {
        return rawValue != null ? rawValue : (typedValue != null ? String.valueOf(typedValue.getData()) : "");
    }
}
