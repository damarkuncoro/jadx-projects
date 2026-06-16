package jadx.gui.ads;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import jadx.gui.ui.MainWindow;
import jadx.gui.utils.UiUtils;

public class FridaScriptDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	private final String scriptContent;

	public FridaScriptDialog(MainWindow mainWindow, String scriptContent) {
		super(mainWindow, "Generated Ad-Block Frida Script", ModalityType.APPLICATION_MODAL);
		this.scriptContent = scriptContent;
		initUI();
		UiUtils.addEscapeShortCutToDispose(this);
	}

	private void initUI() {
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setSize(new Dimension(650, 500));
		setLocationRelativeTo(getParent());

		JPanel panel = new JPanel(new BorderLayout(10, 10));
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel, BorderLayout.CENTER);

		JTextArea textArea = new JTextArea(scriptContent);
		textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
		textArea.setEditable(false);
		textArea.setCaretPosition(0);

		JScrollPane scrollPane = new JScrollPane(textArea);
		panel.add(scrollPane, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));

		JButton copyButton = new JButton("Copy to Clipboard");
		copyButton.addActionListener(e -> {
			try {
				StringSelection selection = new StringSelection(scriptContent);
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				clipboard.setContents(selection, selection);
				JOptionPane.showMessageDialog(this,
						"Frida script successfully copied to clipboard!",
						"Success",
						JOptionPane.INFORMATION_MESSAGE);
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(this,
						"Failed to copy to clipboard: " + ex.getMessage(),
						"Error",
						JOptionPane.ERROR_MESSAGE);
			}
		});
		buttonPanel.add(copyButton);

		JButton closeButton = new JButton("Close");
		closeButton.addActionListener(e -> dispose());
		buttonPanel.add(closeButton);

		panel.add(buttonPanel, BorderLayout.SOUTH);
	}
}
