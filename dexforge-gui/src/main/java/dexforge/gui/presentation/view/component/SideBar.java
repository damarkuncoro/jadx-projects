package dexforge.gui.presentation.view.component;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import com.formdev.flatlaf.extras.components.FlatButton;

public final class SideBar extends JPanel {
    private final Map<String, JToggleButton> buttons = new HashMap<>();
    private final boolean leftSide;

    public SideBar(boolean leftSide) {
        this.leftSide = leftSide;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setPreferredSize(new Dimension(30, 0));
    }

    public void addButton(String title, Runnable onToggle) {
        JToggleButton btn = new JToggleButton(title);
        btn.setFocusable(false);
        btn.putClientProperty("JButton.buttonType", "toolBarButton");

        // Simulating vertical text/look
        btn.setMaximumSize(new Dimension(30, 100));
        btn.setPreferredSize(new Dimension(30, 100));

        btn.addActionListener(e -> onToggle.run());

        add(btn);
        add(Box.createVerticalStrut(2));
        buttons.put(title, btn);
    }
}
