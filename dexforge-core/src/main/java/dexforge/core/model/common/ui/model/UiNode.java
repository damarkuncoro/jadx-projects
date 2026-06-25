package dexforge.core.model.common.ui.model;

import java.util.ArrayList;
import java.util.List;

/**
 * REUSEABLE model for a UI component in the hierarchy.
 * Follows SOLID by decoupling from Android-specific AxmlNode.
 */
public final class UiNode {
    private final String type; // e.g., TextView, Button
    private final List<UiProperty> properties = new ArrayList<>();
    private final List<UiNode> children = new ArrayList<>();

    public UiNode(String type) {
        this.type = type;
    }

    public String getType() { return type; }
    public List<UiProperty> getProperties() { return properties; }
    public List<UiNode> getChildren() { return children; }

    public void addProperty(UiProperty property) {
        properties.add(property);
    }

    public void addChild(UiNode child) {
        children.add(child);
    }
}
