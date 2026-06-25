package dexforge.gui.presentation.view.manager;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JTabbedPane;
import dexforge.gui.presentation.view.component.SideBar;

public final class ToolWindowManager {
    private final List<ToolWindowDescriptor> descriptors = new ArrayList<>();
    private final JTabbedPane leftContainer;
    private final JTabbedPane rightContainer;
    private final JTabbedPane bottomContainer;
    private final SideBar leftBar;
    private final SideBar rightBar;

    public ToolWindowManager(JTabbedPane left, JTabbedPane right, JTabbedPane bottom, SideBar leftBar, SideBar rightBar) {
        this.leftContainer = left;
        this.rightContainer = right;
        this.bottomContainer = bottom;
        this.leftBar = leftBar;
        this.rightBar = rightBar;
    }

    public void register(ToolWindowDescriptor descriptor) {
        descriptors.add(descriptor);
        JTabbedPane container = getContainerFor(descriptor.getAnchor());
        container.addTab(descriptor.getTitle(), descriptor.getComponent());

        // Add toggle button to sidebar
        if (descriptor.getAnchor() == ToolWindowDescriptor.Anchor.LEFT) {
            leftBar.addButton(descriptor.getTitle(), () -> toggle(container));
        } else if (descriptor.getAnchor() == ToolWindowDescriptor.Anchor.RIGHT) {
            rightBar.addButton(descriptor.getTitle(), () -> toggle(container));
        }
    }

    private void toggle(JTabbedPane container) {
        container.setVisible(!container.isVisible());
    }

    private JTabbedPane getContainerFor(ToolWindowDescriptor.Anchor anchor) {
        switch (anchor) {
            case LEFT: return leftContainer;
            case RIGHT: return rightContainer;
            case BOTTOM: return bottomContainer;
            default: return bottomContainer;
        }
    }
}
