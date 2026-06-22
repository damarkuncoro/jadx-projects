package dexforge.gui.presentation.view;

import java.awt.BorderLayout;
import javax.swing.JPanel;

import dexforge.gui.presentation.viewmodel.MainViewModel;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

public final class CodeAreaPanel extends JPanel {
	private final MainViewModel viewModel;
	private final RSyntaxTextArea textArea;

	public CodeAreaPanel(MainViewModel viewModel) {
		this.viewModel = viewModel;
		this.textArea = new RSyntaxTextArea();
		this.textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
		this.textArea.setEditable(false);

		setLayout(new BorderLayout());
		add(new RTextScrollPane(textArea), BorderLayout.CENTER);

		bindViewModel();
	}

	private void bindViewModel() {
		viewModel.onCodeChanged(code -> {
			textArea.setText(code);
			textArea.setCaretPosition(0);
		});
	}
}
