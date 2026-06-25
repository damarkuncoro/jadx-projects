package dexforge.core.parser.axml.service;

import dexforge.core.parser.axml.model.AxmlAttribute;
import dexforge.core.parser.axml.model.AxmlNode;
import java.util.HashMap;
import java.util.Map;

/**
 * Analyzes layout AXML files to extract UI structure and resource usage.
 */
public final class LayoutAnalyzer {
    private final AxmlNode root;

    public LayoutAnalyzer(AxmlNode root) {
        this.root = root;
    }

    public Map<String, Integer> getViewStats() {
        Map<String, Integer> stats = new HashMap<>();
        countViews(root, stats);
        return stats;
    }

    private void countViews(AxmlNode node, Map<String, Integer> stats) {
        stats.put(node.getName(), stats.getOrDefault(node.getName(), 0) + 1);
        for (AxmlNode child : node.getChildren()) {
            countViews(child, stats);
        }
    }

    public String getLayoutPreview() {
        StringBuilder sb = new StringBuilder();
        sb.append("<!-- Layout Preview Analysis -->\n");
        renderNode(root, sb, 0);
        return sb.toString();
    }

    public Map<String, String> getResourceDependencies() {
        Map<String, String> deps = new HashMap<>();
        collectDependencies(root, deps);
        return deps;
    }

    public AxmlNode findViewById(String id) {
        return findNodeById(root, id);
    }

    private AxmlNode findNodeById(AxmlNode node, String id) {
        for (AxmlAttribute attr : node.getAttributes()) {
            if ("id".equals(attr.getName()) && id.equals(attr.getValue())) {
                return node;
            }
        }
        for (AxmlNode child : node.getChildren()) {
            AxmlNode found = findNodeById(child, id);
            if (found != null) return found;
        }
        return null;
    }

    private void collectDependencies(AxmlNode node, Map<String, String> deps) {
        for (AxmlAttribute attr : node.getAttributes()) {
            String val = attr.getValue();
            if (val != null && (val.startsWith("@") || val.startsWith("?"))) {
                deps.put(attr.getName(), val);
            }
        }
        for (AxmlNode child : node.getChildren()) {
            collectDependencies(child, deps);
        }
    }

    private void renderNode(AxmlNode node, StringBuilder sb, int indent) {
        String space = "  ".repeat(indent);
        sb.append(space).append("[").append(node.getName()).append("]");

        for (AxmlAttribute attr : node.getAttributes()) {
            if ("id".equals(attr.getName())) {
                sb.append(" id=").append(attr.getValue());
            } else if ("text".equals(attr.getName())) {
                String text = attr.getValue();
                if (text != null && text.length() > 20) text = text.substring(0, 17) + "...";
                sb.append(" text=\"").append(text).append("\"");
            } else if ("src".equals(attr.getName()) || "background".equals(attr.getName())) {
                sb.append(" ").append(attr.getName()).append("=").append(attr.getValue());
            }
        }
        sb.append("\n");

        for (AxmlNode child : node.getChildren()) {
            renderNode(child, sb, indent + 1);
        }
    }
}
