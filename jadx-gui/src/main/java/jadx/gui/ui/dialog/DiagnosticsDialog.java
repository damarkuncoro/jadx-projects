package jadx.gui.ui.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import dexforge.engine.DexForgeDiagnostic;

import jadx.gui.ui.MainWindow;

public class DiagnosticsDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	public DiagnosticsDialog(JFrame parent, MainWindow mainWindow) {
		super(parent, "DexForge Diagnostics", true);
		setSize(new Dimension(800, 600));
		setLocationRelativeTo(parent);

		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		JTextArea textArea = new JTextArea();
		textArea.setEditable(false);
		textArea.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12));

		StringBuilder sb = new StringBuilder();
		List<DexForgeDiagnostic> diagnostics = mainWindow.getWrapper().getDiagnostics();
		sb.append("Total diagnostics: ").append(diagnostics.size()).append("\n\n");

		for (DexForgeDiagnostic diag : diagnostics) {
			sb.append(String.format("[%s] %s", diag.getSeverity(), diag.getMessage()));
			if (diag.getSource() != null) {
				sb.append("\n  Source: ").append(diag.getSource());
			}
			if (diag.getMethod() != null) {
				sb.append("\n  Method: ").append(diag.getMethod());
			}
			sb.append("\n\n");
		}

		textArea.setText(sb.toString());
		panel.add(new JScrollPane(textArea), BorderLayout.CENTER);
		add(panel);
	}
}
