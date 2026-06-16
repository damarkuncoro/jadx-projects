package jadx.gui.frida;

import java.awt.*;
import java.awt.event.ActionEvent;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import jadx.gui.utils.NLS;

public class AddSnippetDialog extends JDialog {
	private static final long serialVersionUID = -1764883426598335083L;
	private static final int DIALOG_WIDTH = 600;
	private static final int DIALOG_HEIGHT = 400;

	private final JTextField nameField;
	private final JTextArea scriptArea;
	private final String originalSnippetName;
	private String snippetName;
	private String snippetScript;
	private boolean confirmed = false;

	public AddSnippetDialog(Window parent) {
		this(parent, null, null);
	}

	public AddSnippetDialog(Window parent, String initialScript) {
		this(parent, null, initialScript);
	}

	public AddSnippetDialog(Window parent, String originalName, String initialScript) {
		super(parent, originalName == null ? NLS.str("frida.add_snippet") : "Edit Snippet", ModalityType.APPLICATION_MODAL);
		this.originalSnippetName = originalName;
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
		setLocationRelativeTo(parent);

		JPanel panel = new JPanel(new BorderLayout(10, 10));
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel, BorderLayout.CENTER);

		// Name field
		JPanel namePanel = new JPanel(new BorderLayout(5, 0));
		namePanel.add(new JLabel(NLS.str("frida.snippet_name")), BorderLayout.WEST);
		nameField = new JTextField();
		if (originalName != null) {
			nameField.setText(originalName);
		}
		namePanel.add(nameField, BorderLayout.CENTER);
		panel.add(namePanel, BorderLayout.NORTH);

		// Script area
		scriptArea = new JTextArea();
		scriptArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
		if (initialScript != null) {
			scriptArea.setText(initialScript);
		}
		JScrollPane scriptScrollPane = new JScrollPane(scriptArea);
		panel.add(scriptScrollPane, BorderLayout.CENTER);

		// Buttons
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
		JButton okButton = new JButton(NLS.str("dialog.ok"));
		okButton.addActionListener(this::onOkButtonClicked);
		getRootPane().setDefaultButton(okButton);
		buttonPanel.add(okButton);

		JButton cancelButton = new JButton(NLS.str("dialog.cancel"));
		cancelButton.addActionListener(this::onCancelButtonClicked);
		buttonPanel.add(cancelButton);

		panel.add(buttonPanel, BorderLayout.SOUTH);
	}

	public String getOriginalSnippetName() {
		return originalSnippetName;
	}

	private void onOkButtonClicked(ActionEvent e) {
		snippetName = nameField.getText().trim();
		if (snippetName.isEmpty()) {
			JOptionPane.showMessageDialog(this,
					NLS.str("frida.snippet_name_required"),
					NLS.str("dialog.error"),
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		snippetScript = scriptArea.getText();
		confirmed = true;
		dispose();
	}

	private void onCancelButtonClicked(ActionEvent e) {
		confirmed = false;
		dispose();
	}

	public String getSnippetName() {
		return snippetName;
	}

	public String getSnippetScript() {
		return snippetScript;
	}

	public boolean isConfirmed() {
		return confirmed;
	}
}
