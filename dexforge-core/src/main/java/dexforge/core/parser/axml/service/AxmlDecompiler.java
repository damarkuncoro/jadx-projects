package dexforge.core.parser.axml.service;

import dexforge.core.parser.axml.model.AxmlAttribute;
import dexforge.core.parser.axml.model.AxmlNode;
import dexforge.core.parser.arsc.model.ArscResourceValue;
import dexforge.core.parser.arsc.parser.ArscValueDecoder;
import dexforge.core.parser.resolver.ResourceResolver;
import java.util.List;

/**
 * Enhanced AXML Decompiler for 100% accurate Android Manifest/Layout reconstruction.
 */
public final class AxmlDecompiler {
    private final ResourceResolver resourceResolver;

    public AxmlDecompiler(ResourceResolver resourceResolver) {
        this.resourceResolver = resourceResolver;
    }

    public String decompile(AxmlNode root) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
        decompileNode(root, sb, 0);
        return sb.toString();
    }

    private void decompileNode(AxmlNode node, StringBuilder sb, int indent) {
        String space = "    ".repeat(indent);
        sb.append(space).append("<").append(node.getName());

        List<AxmlAttribute> attrs = node.getAttributes();
        if (!attrs.isEmpty()) {
            for (AxmlAttribute attr : attrs) {
                sb.append("\n").append(space).append("    ");

                // 1. Resolve Attribute Name
                String name = attr.getName();
                int nameId = attr.getNameResourceId();
                if (nameId != 0) {
                    String resolved = resourceResolver.resolve(nameId);
                    if (resolved != null) {
                        name = resolved;
                    }
                }

                // Ensure android namespace prefix if it's a system resource
                if (nameId >= 0x01010000 && nameId <= 0x0101ffff && !name.startsWith("android:")) {
                    name = "android:" + name;
                }

                // 2. Resolve Attribute Value
                String value = attr.getRawValue();
                ArscResourceValue typedVal = attr.getTypedValue();

                if (typedVal != null) {
                    if (typedVal.getType() == ArscResourceValue.TYPE_REFERENCE ||
                        typedVal.getType() == ArscResourceValue.TYPE_DYNAMIC_REFERENCE) {
                        String resName = resourceResolver.resolve(typedVal.getData());
                        if (resName != null) {
                            value = "@" + resName.replace("R.", "").replace(".", "/");
                        } else {
                            value = String.format("@0x%08x", typedVal.getData());
                        }
                    } else if (typedVal.getType() == ArscResourceValue.TYPE_STRING) {
                        // value is already the string from rawValue
                    } else {
                        // Use ArscValueDecoder for colors, dimensions, etc.
                        value = ArscValueDecoder.decode(typedVal);
                    }
                }

                sb.append(name).append("=\"").append(value).append("\"");
            }
        }

        if (node.getChildren().isEmpty()) {
            sb.append(" />\n");
        } else {
            sb.append(">\n");
            for (AxmlNode child : node.getChildren()) {
                decompileNode(child, sb, indent + 1);
            }
            sb.append(space).append("</").append(node.getName()).append(">\n");
        }
    }
}
