package dexforge.core.parser.axml.model;

import java.util.ArrayList;
import java.util.List;

/**
 * REUSEABLE model for AXML elements.
 */
public final class AxmlNode {
    private String name;
    private final List<AxmlAttribute> attributes = new ArrayList<>();
    private final List<AxmlNode> children = new ArrayList<>();

    public AxmlNode(String name) {
        this.name = name;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<AxmlAttribute> getAttributes() { return attributes; }
    public List<AxmlNode> getChildren() { return children; }

    public void addAttribute(AxmlAttribute attr) {
        attributes.add(attr);
    }

    public void removeAttribute(String attrName) {
        attributes.removeIf(a -> a.getName().equals(attrName));
    }

    public void addChild(AxmlNode child) {
        children.add(child);
    }

    public void removeChild(AxmlNode child) {
        children.remove(child);
    }
}
