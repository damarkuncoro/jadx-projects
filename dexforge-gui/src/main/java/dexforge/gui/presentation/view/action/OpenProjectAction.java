package dexforge.gui.presentation.view.action;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.JFileChooser;
import javax.swing.KeyStroke;
import dexforge.gui.presentation.viewmodel.MainViewModel;

public final class OpenProjectAction extends DexForgeAction {
    private final MainViewModel viewModel;
    private final Component parent;

    public OpenProjectAction(MainViewModel viewModel, Component parent) {
        super("Open File...");
        this.viewModel = viewModel;
        this.parent = parent;
        setShortcut(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK));
        setDescription("Open APK, DEX, or JAR file");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
            viewModel.openFile(chooser.getSelectedFile());
        }
    }
}
