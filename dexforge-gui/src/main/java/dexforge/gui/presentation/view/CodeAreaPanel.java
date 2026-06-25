package dexforge.gui.presentation.view;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import dexforge.gui.presentation.viewmodel.MainViewModel;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

public class CodeAreaPanel extends JPanel {
	private final MainViewModel viewModel;
	private final RSyntaxTextArea textArea;

	public CodeAreaPanel(MainViewModel viewModel) {
		this(viewModel, SyntaxConstants.SYNTAX_STYLE_JAVA);
	}

	public CodeAreaPanel(MainViewModel viewModel, String syntaxStyle) {
		this.viewModel = viewModel;
		this.textArea = new RSyntaxTextArea();
		this.textArea.setSyntaxEditingStyle(syntaxStyle);
		this.textArea.setEditable(false);

		setLayout(new BorderLayout());
		add(new RTextScrollPane(textArea), BorderLayout.CENTER);
	}

	public void setText(String code) {
		SwingUtilities.invokeLater(() -> {
			textArea.setText(code);
			textArea.setCaretPosition(0);
		});
	}
}
