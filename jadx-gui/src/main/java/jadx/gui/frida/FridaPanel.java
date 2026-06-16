package jadx.gui.frida;

import java.awt.*;
import java.awt.event.ActionEvent;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jadx.api.JavaMethod;
import jadx.frida.*;
import jadx.gui.ui.MainWindow;

public class FridaPanel extends JPanel {
	private static final Logger LOG = LoggerFactory.getLogger(FridaPanel.class);
	private static final String SELECT_SNIPPET_TEXT = "Select a snippet...";

	private final MainWindow mainWindow;
	private final IFridaProcessExecutor processExecutor;
	private final IFridaScriptGenerator scriptGenerator;
	private final FridaSnippetRegistry snippetRegistry;

	private RSyntaxTextArea scriptTextArea;
	private JTextArea logTextArea;
	private JButton runButton;
	private JComboBox<String> snippetsComboBox;

	public FridaPanel(MainWindow mainWindow) {
		this.mainWindow = mainWindow;
		this.processExecutor = new FridaProcessExecutor();
		this.scriptGenerator = new FridaScriptGenerator();
		this.snippetRegistry = new FridaSnippetRegistry();
		this.snippetRegistry.registerDefaultSnippets();
		initUI();
	}

	private void initUI() {
		setLayout(new BorderLayout(10, 10));
		setBorder(new EmptyBorder(10, 10, 10, 10));

		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPane.setResizeWeight(0.5);
		add(splitPane, BorderLayout.CENTER);

		splitPane.setTopComponent(createScriptPanel());
		splitPane.setBottomComponent(createLogPanel());
	}

	private JPanel createScriptPanel() {
		JPanel scriptPanel = new JPanel(new BorderLayout(5, 5));

		// Top panel with title and snippets dropdown
		JPanel topPanel = new JPanel(new BorderLayout(5, 5));

		JLabel scriptLabel = new JLabel("Frida Integration");
		scriptLabel.setFont(scriptLabel.getFont().deriveFont(Font.BOLD, 16f));
		topPanel.add(scriptLabel, BorderLayout.NORTH);

		JPanel snippetsPanel = new JPanel(new BorderLayout(5, 5));
		JLabel snippetsLabel = new JLabel("Predefined Snippets:");
		snippetsPanel.add(snippetsLabel, BorderLayout.WEST);

		snippetsComboBox = new JComboBox<>();
		snippetsComboBox.addItem(SELECT_SNIPPET_TEXT);
		for (IFridaSnippet snippet : snippetRegistry.getAllSnippets()) {
			snippetsComboBox.addItem(snippet.getDisplayName());
		}
		snippetsComboBox.addActionListener(this::onSnippetSelected);
		snippetsPanel.add(snippetsComboBox, BorderLayout.CENTER);

		topPanel.add(snippetsPanel, BorderLayout.SOUTH);
		scriptPanel.add(topPanel, BorderLayout.NORTH);

		// Script editor
		scriptTextArea = new RSyntaxTextArea(20, 80);
		scriptTextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT);
		scriptTextArea.setCodeFoldingEnabled(true);
		scriptTextArea.setAntiAliasingEnabled(true);
		RTextScrollPane scriptScrollPane = new RTextScrollPane(scriptTextArea);
		scriptPanel.add(scriptScrollPane, BorderLayout.CENTER);

		// Buttons
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		runButton = new JButton("Run Frida Script");
		runButton.setToolTipText("Execute the current Frida script");
		runButton.addActionListener(this::onRunButtonClicked);
		buttonPanel.add(runButton);

		JButton clearScriptButton = new JButton("Clear Script");
		clearScriptButton.addActionListener(e -> scriptTextArea.setText(""));
		buttonPanel.add(clearScriptButton);

		scriptPanel.add(buttonPanel, BorderLayout.SOUTH);

		return scriptPanel;
	}

	private JPanel createLogPanel() {
		JPanel logPanel = new JPanel(new BorderLayout(5, 5));

		JLabel logLabel = new JLabel("Frida Output Log:");
		logLabel.setFont(logLabel.getFont().deriveFont(Font.BOLD));
		logPanel.add(logLabel, BorderLayout.NORTH);

		logTextArea = new JTextArea(10, 80);
		logTextArea.setEditable(false);
		logTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
		JScrollPane logScrollPane = new JScrollPane(logTextArea);
		logPanel.add(logScrollPane, BorderLayout.CENTER);

		JPanel logButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JButton clearLogButton = new JButton("Clear Log");
		clearLogButton.addActionListener(e -> logTextArea.setText(""));
		logButtonPanel.add(clearLogButton);
		logPanel.add(logButtonPanel, BorderLayout.SOUTH);

		return logPanel;
	}

	private void onSnippetSelected(ActionEvent e) {
		String selected = (String) snippetsComboBox.getSelectedItem();
		if (selected != null && !SELECT_SNIPPET_TEXT.equals(selected)) {
			snippetRegistry.findByDisplayName(selected).ifPresent(snippet -> {
				scriptTextArea.setText(snippet.getScript());
				appendLog("[INFO] Loaded snippet: " + selected);
			});
		}
	}

	private void onRunButtonClicked(ActionEvent e) {
		String script = scriptTextArea.getText().trim();
		if (script.isEmpty()) {
			JOptionPane.showMessageDialog(this,
					"Please enter a Frida script first",
					"Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		String target = JOptionPane.showInputDialog(this,
				"Enter target process name or package name (e.g., com.example.app):",
				"Run Frida Script",
				JOptionPane.QUESTION_MESSAGE);

		if (target == null || target.trim().isEmpty()) {
			return;
		}

		runFridaScript(target.trim(), script);
	}

	public void generateAndDisplayScript(JavaMethod method) {
		try {
			String script = scriptGenerator.generateMethodHook(method);
			scriptTextArea.setText(script);
			appendLog("[INFO] Generated Frida script for method: " + method.getFullName());
		} catch (Exception e) {
			LOG.error("Failed to generate Frida script", e);
			appendLog("[ERROR] Failed to generate Frida script: " + e.getMessage());
		}
	}

	private void runFridaScript(String target, String script) {
		runButton.setEnabled(false);

		new Thread(() -> {
			try {
				processExecutor.execute(target, script, this::appendLog);
			} catch (Exception e) {
				LOG.error("Failed to run Frida script", e);
				SwingUtilities.invokeLater(() -> appendLog("[ERROR] " + e.getMessage()));
			} finally {
				SwingUtilities.invokeLater(() -> runButton.setEnabled(true));
			}
		}).start();
	}

	private void appendLog(String line) {
		SwingUtilities.invokeLater(() -> {
			logTextArea.append(line + "\n");
			logTextArea.setCaretPosition(logTextArea.getDocument().getLength());
		});
	}

	public void stopFridaProcess() {
		processExecutor.stop();
	}
}
