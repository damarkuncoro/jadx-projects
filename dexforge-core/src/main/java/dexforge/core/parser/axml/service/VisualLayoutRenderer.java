package dexforge.core.parser.axml.service;

import dexforge.api.intelligence.IVisualLayout;
import dexforge.core.model.common.ui.model.UiNode;
import dexforge.core.model.common.ui.model.UiProperty;
import dexforge.core.parser.axml.model.AxmlNode;
import dexforge.core.parser.resolver.ResourceResolver;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

/**
 * Enhanced Renderer for Visual Layout.
 * SOLID & DRY: delegates transformation to LayoutToUiTransformer.
 */
public final class VisualLayoutRenderer implements IVisualLayout {
    private final LayoutToUiTransformer transformer;

    public VisualLayoutRenderer(ResourceResolver resourceResolver) {
        this.transformer = new LayoutToUiTransformer(resourceResolver);
    }

    @Override
    public Map<String, Object> renderLayout(Object layoutNode) {
        if (layoutNode instanceof AxmlNode) {
            UiNode uiTree = transformer.transform((AxmlNode) layoutNode);
            return convertToMap(uiTree);
        }
        return new HashMap<>();
    }

    private Map<String, Object> convertToMap(UiNode node) {
        if (node == null) return new HashMap<>();

        Map<String, Object> map = new HashMap<>();
        map.put("type", node.getType());
        map.put("id", System.identityHashCode(node) + ""); // Stable ID for editor mapping

        Map<String, String> properties = new HashMap<>();
        for (UiProperty prop : node.getProperties()) {
            properties.put(prop.getName(), prop.getResolvedValue());
        }
        map.put("properties", properties);

        List<Map<String, Object>> children = new ArrayList<>();
        for (UiNode child : node.getChildren()) {
            children.add(convertToMap(child));
        }
        map.put("children", children);

        return map;
    }
}
