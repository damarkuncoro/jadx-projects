package jadx.gui.frida;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jadx.api.JavaMethod;
import jadx.frida.*;
import jadx.gui.settings.JadxSettings;
import jadx.gui.ui.MainWindow;
import jadx.gui.ui.action.JadxAutoCompletion;

public class FridaPanel extends JPanel {
	private static final Logger LOG = LoggerFactory.getLogger(FridaPanel.class);
	private static final String SELECT_SNIPPET_TEXT = "Select a snippet...";

	private final MainWindow mainWindow;
	private final JadxSettings settings;
	private final IFridaProcessExecutor processExecutor;
	private final IFridaScriptGenerator scriptGenerator;
	private final FridaSnippetRegistry snippetRegistry;

	private RSyntaxTextArea scriptTextArea;
	private JTextArea logTextArea;
	private JButton runButton;
	private JComboBox<String> snippetsComboBox;

	public FridaPanel(MainWindow mainWindow) {
		this.mainWindow = mainWindow;
		this.settings = mainWindow.getSettings();
		this.processExecutor = new FridaProcessExecutor();
		this.scriptGenerator = new FridaScriptGenerator();
		this.snippetRegistry = new FridaSnippetRegistry();
		this.snippetRegistry.registerDefaultSnippets();
		loadCustomSnippets();
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
		refreshSnippetsComboBox();
		snippetsComboBox.addActionListener(this::onSnippetSelected);
		snippetsPanel.add(snippetsComboBox, BorderLayout.CENTER);

		JButton addSnippetButton = new JButton("Add Custom Snippet");
		addSnippetButton.addActionListener(this::onAddSnippetButtonClicked);
		snippetsPanel.add(addSnippetButton, BorderLayout.EAST);

		topPanel.add(snippetsPanel, BorderLayout.SOUTH);
		scriptPanel.add(topPanel, BorderLayout.NORTH);

		// Script editor
		scriptTextArea = new RSyntaxTextArea(20, 80);
		scriptTextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT);
		scriptTextArea.setCodeFoldingEnabled(true);
		scriptTextArea.setAntiAliasingEnabled(true);

		// Add Frida API auto-completion
		AutoCompletion ac = new JadxAutoCompletion(FridaApiCompletionProvider.create());
		ac.install(scriptTextArea);

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

		JButton saveScriptButton = new JButton("Save Script");
		saveScriptButton.addActionListener(this::onSaveScriptClicked);
		buttonPanel.add(saveScriptButton);

		JButton loadScriptButton = new JButton("Load Script");
		loadScriptButton.addActionListener(this::onLoadScriptClicked);
		buttonPanel.add(loadScriptButton);

		scriptPanel.add(buttonPanel, BorderLayout.SOUTH);

		return scriptPanel;
	}

	private void refreshSnippetsComboBox() {
		snippetsComboBox.removeAllItems();
		snippetsComboBox.addItem(SELECT_SNIPPET_TEXT);
		for (IFridaSnippet snippet : snippetRegistry.getAllSnippets()) {
			snippetsComboBox.addItem(snippet.getDisplayName());
		}
	}

	private void loadCustomSnippets() {
		List<CustomFridaSnippet> customSnippets = settings.getCustomFridaSnippets();
		for (CustomFridaSnippet custom : customSnippets) {
			IFridaSnippet snippet = new IFridaSnippet() {
				@Override
				public String getDisplayName() {
					return custom.getName();
				}

				@Override
				public String getScript() {
					return custom.getScript();
				}
			};
			snippetRegistry.registerSnippet(snippet);
		}
	}

	private void onAddSnippetButtonClicked(ActionEvent e) {
		AddSnippetDialog dialog = new AddSnippetDialog(mainWindow);
		dialog.setVisible(true);
		if (dialog.isConfirmed()) {
			String name = dialog.getSnippetName();
			String script = dialog.getSnippetScript();

			IFridaSnippet newSnippet = new IFridaSnippet() {
				@Override
				public String getDisplayName() {
					return name;
				}

				@Override
				public String getScript() {
					return script;
				}
			};
			snippetRegistry.registerSnippet(newSnippet);

			List<CustomFridaSnippet> customSnippets = new ArrayList<>(settings.getCustomFridaSnippets());
			customSnippets.add(new CustomFridaSnippet(name, script));
			settings.setCustomFridaSnippets(customSnippets);

			refreshSnippetsComboBox();
			appendLog("[INFO] Added custom snippet: " + name);
		}
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

	private void onSaveScriptClicked(ActionEvent e) {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Save Frida Script");
		fileChooser.setFileFilter(new FileNameExtensionFilter("Frida Scripts (*.js)", "js"));
		int userSelection = fileChooser.showSaveDialog(this);

		if (userSelection == JFileChooser.APPROVE_OPTION) {
			File fileToSave = fileChooser.getSelectedFile();
			if (!fileToSave.getName().toLowerCase().endsWith(".js")) {
				fileToSave = new File(fileToSave.getAbsolutePath() + ".js");
			}

			try {
				Files.write(fileToSave.toPath(), scriptTextArea.getText().getBytes(StandardCharsets.UTF_8));
				appendLog("[INFO] Saved script to: " + fileToSave.getAbsolutePath());
			} catch (IOException ex) {
				LOG.error("Failed to save script", ex);
				JOptionPane.showMessageDialog(this,
						"Failed to save script: " + ex.getMessage(),
						"Error",
						JOptionPane.ERROR_MESSAGE);
				appendLog("[ERROR] Failed to save script: " + ex.getMessage());
			}
		}
	}

	private void onLoadScriptClicked(ActionEvent e) {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Load Frida Script");
		fileChooser.setFileFilter(new FileNameExtensionFilter("Frida Scripts (*.js)", "js"));
		int userSelection = fileChooser.showOpenDialog(this);

		if (userSelection == JFileChooser.APPROVE_OPTION) {
			File fileToLoad = fileChooser.getSelectedFile();

			try {
				String content = Files.readString(fileToLoad.toPath(), StandardCharsets.UTF_8);
				scriptTextArea.setText(content);
				appendLog("[INFO] Loaded script from: " + fileToLoad.getAbsolutePath());
			} catch (IOException ex) {
				LOG.error("Failed to load script", ex);
				JOptionPane.showMessageDialog(this,
						"Failed to load script: " + ex.getMessage(),
						"Error",
						JOptionPane.ERROR_MESSAGE);
				appendLog("[ERROR] Failed to load script: " + ex.getMessage());
			}
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
