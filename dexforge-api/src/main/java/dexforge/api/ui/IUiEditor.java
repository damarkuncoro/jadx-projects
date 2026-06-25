package dexforge.api.ui;

import java.util.Map;

/**
 * REUSEABLE API for UI modification.
 * Decouples GUI from specific UI XML implementations.
 */
public interface IUiEditor {
    /**
     * Updates a property of a UI component.
     */
    void updateProperty(Object node, String propertyName, String newValue);

    /**
     * Deletes a UI component.
     */
    void deleteNode(Object node);

    /**
     * Adds a child UI component.
     */
    void addChild(Object parentNode, String type);

    /**
     * Commits all changes and returns the modified UI in its source format (e.g., AXML bytes).
     */
    byte[] commitChanges();
}
