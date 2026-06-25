package dexforge.gui.presentation.view.inspector;

import javax.swing.*;
import java.awt.*;
import dexforge.gui.presentation.viewmodel.MainViewModel;

/**
 * REUSEABLE: Inspector panel for method details and risk flags.
 */
public final class MethodInfoPanel extends JPanel {
    private final MainViewModel viewModel;

    public MethodInfoPanel(MainViewModel viewModel) {
        this.viewModel = viewModel;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        add(createSection("Method Signatures"));
        add(createSection("Register State (DFA)"));
        add(createSection("Risk Flags (Security)"));
    }

    private JPanel createSection(String title) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.add(new JLabel("No selection"), BorderLayout.CENTER);
        return p;
    }
}
