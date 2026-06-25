package dexforge.gui.presentation.view.editor;

import dexforge.gui.presentation.view.CodeAreaPanel;
import dexforge.gui.presentation.viewmodel.MainViewModel;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

public final class SmaliPanel extends CodeAreaPanel {
    public SmaliPanel(MainViewModel viewModel) {
        super(viewModel, SyntaxConstants.SYNTAX_STYLE_NONE); // No smali style in RSyntaxTextArea by default

        viewModel.onSmaliChanged(this::setText);
    }
}
