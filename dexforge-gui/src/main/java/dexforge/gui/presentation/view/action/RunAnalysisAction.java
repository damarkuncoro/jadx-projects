package dexforge.gui.presentation.view.action;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;
import dexforge.gui.presentation.viewmodel.MainViewModel;

public final class RunAnalysisAction extends DexForgeAction {
    private final MainViewModel viewModel;

    public RunAnalysisAction(MainViewModel viewModel) {
        super("Run Deep Analysis");
        this.viewModel = viewModel;
        setShortcut(KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0));
        setDescription("Run automated security analysis");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        viewModel.runDeepAnalysis();
    }
}
