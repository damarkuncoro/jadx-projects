package dexforge.gui.presentation.view.editor.ui;

import dexforge.gui.presentation.viewmodel.MainViewModel;
import javax.swing.*;
import java.awt.*;
import java.util.Map;

/**
 * REUSEABLE: GUI panel for visual APK UI editing.
 * SOLID: focuses only on rendering the UI tree and property editing.
 */
public final class VisualUiEditorPanel extends JPanel {
    private final MainViewModel viewModel;
    private final JTree uiTree = new JTree();
    private final JTable propertyTable = new JTable();

    public VisualUiEditorPanel(MainViewModel viewModel) {
        this.viewModel = viewModel;
        setLayout(new BorderLayout());
        initUi();
    }

    private void initUi() {
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(new JScrollPane(uiTree));
        splitPane.setRightComponent(new JScrollPane(propertyTable));
        splitPane.setDividerLocation(300);

        add(splitPane, BorderLayout.CENTER);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.add(new JButton("Save Changes"));
        toolbar.add(new JButton("Refresh Preview"));
        add(toolbar, BorderLayout.NORTH);
    }
}
