package dexforge.gui.presentation.view.manager;

import javax.swing.Icon;
import javax.swing.JComponent;

public final class ToolWindowDescriptor {
    public enum Anchor { LEFT, RIGHT, BOTTOM }

    private final String id;
    private final String title;
    private final JComponent component;
    private final Anchor anchor;
    private final Icon icon;

    public ToolWindowDescriptor(String id, String title, JComponent component, Anchor anchor) {
        this(id, title, component, anchor, null);
    }

    public ToolWindowDescriptor(String id, String title, JComponent component, Anchor anchor, Icon icon) {
        this.id = id;
        this.title = title;
        this.component = component;
        this.anchor = anchor;
        this.icon = icon;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public JComponent getComponent() { return component; }
    public Anchor getAnchor() { return anchor; }
    public Icon getIcon() { return icon; }
}
