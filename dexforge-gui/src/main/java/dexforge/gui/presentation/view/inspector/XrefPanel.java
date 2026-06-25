package dexforge.gui.presentation.view.inspector;

import javax.swing.*;
import java.awt.*;
import dexforge.gui.presentation.viewmodel.MainViewModel;

/**
 * REUSEABLE: Inspector panel for Cross-References (XREF).
 */
public final class XrefPanel extends JPanel {
    private final MainViewModel viewModel;
    private final JTabbedPane tabs = new JTabbedPane();

    public XrefPanel(MainViewModel viewModel) {
        this.viewModel = viewModel;
        setLayout(new BorderLayout());

        tabs.addTab("Called By", new JScrollPane(new JList<String>()));
        tabs.addTab("Calls", new JScrollPane(new JList<String>()));
        tabs.addTab("String Usage", new JScrollPane(new JList<String>()));
        tabs.addTab("Field Usage", new JScrollPane(new JList<String>()));

        add(tabs, BorderLayout.CENTER);
    }
}
