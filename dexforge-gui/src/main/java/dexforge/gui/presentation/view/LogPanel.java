package dexforge.gui.presentation.view;

import java.awt.BorderLayout;
import java.awt.Font;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import dexforge.gui.presentation.viewmodel.MainViewModel;

/**
 * Panel to display application logs.
 */
public class LogPanel extends JPanel {
	private final JTextArea logArea;

	public LogPanel(MainViewModel viewModel) {
		setLayout(new BorderLayout());

		logArea = new JTextArea();
		logArea.setEditable(false);
		logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

		add(new JScrollPane(logArea), BorderLayout.CENTER);

		viewModel.onLogReceived(message -> {
			SwingUtilities.invokeLater(() -> {
				logArea.append(message + "\n");
				logArea.setCaretPosition(logArea.getDocument().getLength());
			});
		});
	}
}
