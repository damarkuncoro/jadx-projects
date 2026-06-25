package dexforge.core.parser.axml.service;

import dexforge.core.model.common.ui.model.UiNode;
import dexforge.core.model.common.ui.model.UiProperty;
import dexforge.core.parser.axml.model.AxmlAttribute;
import dexforge.core.parser.axml.model.AxmlNode;
import dexforge.core.parser.arsc.model.ArscResourceValue;
import dexforge.core.parser.arsc.parser.ArscValueDecoder;
import dexforge.core.parser.resolver.ResourceResolver;

/**
 * SOLID service to transform AXML models into generic UI models.
 * Handles resource resolution during transformation.
 */
public final class LayoutToUiTransformer {
    private final ResourceResolver resourceResolver;

    public LayoutToUiTransformer(ResourceResolver resourceResolver) {
        this.resourceResolver = resourceResolver;
    }

    public UiNode transform(AxmlNode axmlNode) {
        if (axmlNode == null) return null;

        UiNode uiNode = new UiNode(axmlNode.getName());

        for (AxmlAttribute attr : axmlNode.getAttributes()) {
            String name = resolveAttributeName(attr);
            String value = attr.getValue();
            String resolvedValue = resolveAttributeValue(attr);

            uiNode.addProperty(new UiProperty(name, value, resolvedValue));
        }

        for (AxmlNode child : axmlNode.getChildren()) {
            uiNode.addChild(transform(child));
        }

        return uiNode;
    }

    private String resolveAttributeName(AxmlAttribute attr) {
        int nameId = attr.getNameResourceId();
        if (nameId != 0) {
            String resolved = resourceResolver.resolve(nameId);
            if (resolved != null) return resolved;
        }
        return attr.getName();
    }

    private String resolveAttributeValue(AxmlAttribute attr) {
        ArscResourceValue typedVal = attr.getTypedValue();
        if (typedVal == null) return attr.getValue();

        if (typedVal.getType() == ArscResourceValue.TYPE_REFERENCE ||
            typedVal.getType() == ArscResourceValue.TYPE_DYNAMIC_REFERENCE) {
            String resName = resourceResolver.resolve(typedVal.getData());
            return resName != null ? "@" + resName.replace("R.", "").replace(".", "/") : attr.getValue();
        }

        return ArscValueDecoder.decode(typedVal);
    }
}
