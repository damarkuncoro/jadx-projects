package dexforge.core.service.ui;

import dexforge.api.ui.IUiEditor;
import dexforge.core.parser.axml.model.AxmlAttribute;
import dexforge.core.parser.axml.model.AxmlNode;
import dexforge.core.parser.axml.service.AxmlWriter;
import dexforge.core.parser.resolver.ResourceResolver;
import java.util.HashMap;
import java.util.Map;

/**
 * SOLID implementation of UI Editor Service.
 * Acts as a bridge between the generic UI model and Android AXML.
 */
public final class VisualUiEditorService implements IUiEditor {
    private final AxmlNode rootNode;
    private final ResourceResolver resourceResolver;
    private final Map<String, AxmlNode> nodeRegistry = new HashMap<>();

    public VisualUiEditorService(AxmlNode rootNode, ResourceResolver resourceResolver) {
        this.rootNode = rootNode;
        this.resourceResolver = resourceResolver;
        indexNodes(rootNode);
    }

    private void indexNodes(AxmlNode node) {
        // Simple indexing by name+id for demo, in real app would use unique UUIDs
        nodeRegistry.put(System.identityHashCode(node) + "", node);
        for (AxmlNode child : node.getChildren()) {
            indexNodes(child);
        }
    }

    @Override
    public void updateProperty(Object nodeId, String propertyName, String newValue) {
        AxmlNode node = nodeRegistry.get(nodeId.toString());
        if (node != null) {
            node.removeAttribute(propertyName);
            // Re-adding with new value (simplified)
            node.addAttribute(new AxmlAttribute(propertyName, newValue, 0, null));
        }
    }

    @Override
    public void deleteNode(Object nodeId) {
        AxmlNode node = nodeRegistry.get(nodeId.toString());
        if (node != null) {
            findAndRemove(rootNode, node);
        }
    }

    @Override
    public void addChild(Object parentId, String type) {
        AxmlNode parent = nodeRegistry.get(parentId.toString());
        if (parent != null) {
            AxmlNode child = new AxmlNode(type);
            parent.addChild(child);
            nodeRegistry.put(System.identityHashCode(child) + "", child);
        }
    }

    @Override
    public byte[] commitChanges() {
        AxmlWriter writer = new AxmlWriter();
        return writer.write(rootNode);
    }

    private boolean findAndRemove(AxmlNode parent, AxmlNode target) {
        if (parent.getChildren().remove(target)) return true;
        for (AxmlNode child : parent.getChildren()) {
            if (findAndRemove(child, target)) return true;
        }
        return false;
    }
}
