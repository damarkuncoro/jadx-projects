package dexforge.gui.presentation.view.plugin;

import dexforge.api.plugin.IDexForgePlugin;
import dexforge.gui.presentation.viewmodel.MainViewModel;
import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * REUSEABLE: GUI panel to manage DexForge plugins.
 * Follows SOLID by separating plugin view from main window.
 */
public final class PluginManagerPanel extends JPanel {
    private final MainViewModel viewModel;
    private final DefaultListModel<IDexForgePlugin> listModel = new DefaultListModel<>();
    private final JList<IDexForgePlugin> pluginList = new JList<>(listModel);

    public PluginManagerPanel(MainViewModel viewModel) {
        this.viewModel = viewModel;
        setLayout(new BorderLayout());
        initUi();
        setupListeners();
    }

    private void initUi() {
        pluginList.setCellRenderer(new PluginListRenderer());
        add(new JScrollPane(pluginList), BorderLayout.CENTER);

        JPanel controls = new JPanel();
        JButton toggleBtn = new JButton("Toggle On/Off");
        toggleBtn.addActionListener(e -> toggleSelectedPlugin());
        controls.add(toggleBtn);
        add(controls, BorderLayout.SOUTH);
    }

    private void setupListeners() {
        viewModel.onPluginsLoaded(plugins -> {
            listModel.clear();
            plugins.forEach(listModel::addElement);
        });
    }

    private void toggleSelectedPlugin() {
        IDexForgePlugin selected = pluginList.getSelectedValue();
        if (selected != null && selected.getName().contains("JADX")) {
            boolean currentState = true; // Placeholder for actual state check
            viewModel.toggleJadxEngine(!currentState);
            JOptionPane.showMessageDialog(this, "JADX Engine toggled. Re-open file to apply changes.");
        } else {
            JOptionPane.showMessageDialog(this, "Select JADX Engine to toggle.");
        }
    }

    private static class PluginListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof IDexForgePlugin) {
                IDexForgePlugin p = (IDexForgePlugin) value;
                setText(p.getName() + " v" + p.getVersion() + " - " + p.getDescription());
            }
            return this;
        }
    }
}
