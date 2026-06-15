package jadx.gui.frida;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jadx.api.JavaMethod;
import jadx.frida.FridaSnippets;
import jadx.gui.ui.MainWindow;

/**
 * Frida integration panel for Jadx GUI
 */
public class FridaPanel extends JPanel {
	private static final Logger LOG = LoggerFactory.getLogger(FridaPanel.class);

	private final MainWindow mainWindow;
	private RSyntaxTextArea scriptTextArea;
	private JTextArea logTextArea;
	private JButton runButton;
	private JButton clearLogButton;
	private JComboBox<String> snippetsComboBox;
	private Process currentFridaProcess;

	public FridaPanel(MainWindow mainWindow) {
		this.mainWindow = mainWindow;
		initUI();
	}

	private void initUI() {
		setLayout(new BorderLayout(10, 10));
		setBorder(new EmptyBorder(10, 10, 10, 10));

		// Center panel: split between script editor and log
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPane.setResizeWeight(0.5);
		add(splitPane, BorderLayout.CENTER);

		// Script editor panel
		JPanel scriptPanel = new JPanel(new BorderLayout(5, 5));
		
		// Top panel: title + snippets dropdown
		JPanel topPanel = new JPanel(new BorderLayout(5, 5));
		JLabel scriptLabel = new JLabel("Frida Integration");
		scriptLabel.setFont(scriptLabel.getFont().deriveFont(Font.BOLD, 16f));
		topPanel.add(scriptLabel, BorderLayout.NORTH);

		// Snippets dropdown panel
		JPanel snippetsPanel = new JPanel(new BorderLayout(5, 5));
		JLabel snippetsLabel = new JLabel("Predefined Snippets:");
		snippetsLabel.setFont(snippetsLabel.getFont().deriveFont(Font.PLAIN));
		snippetsPanel.add(snippetsLabel, BorderLayout.WEST);

		snippetsComboBox = new JComboBox<>();
		snippetsComboBox.addItem("Select a snippet...");
		for (String name : FridaSnippets.SNIPPETS.keySet()) {
			snippetsComboBox.addItem(name);
		}
		snippetsComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String selected = (String) snippetsComboBox.getSelectedItem();
				if (selected != null && !selected.equals("Select a snippet...")) {
					scriptTextArea.setText(FridaSnippets.SNIPPETS.get(selected));
					logTextArea.append("[INFO] Loaded snippet: " + selected + "\n");
				}
			}
		});
		snippetsPanel.add(snippetsComboBox, BorderLayout.CENTER);
		topPanel.add(snippetsPanel, BorderLayout.SOUTH);
		scriptPanel.add(topPanel, BorderLayout.NORTH);

		scriptTextArea = new RSyntaxTextArea(20, 80);
		scriptTextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT);
		scriptTextArea.setCodeFoldingEnabled(true);
		scriptTextArea.setAntiAliasingEnabled(true);
		RTextScrollPane scriptScrollPane = new RTextScrollPane(scriptTextArea);
		scriptPanel.add(scriptScrollPane, BorderLayout.CENTER);

		// Button panel
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		runButton = new JButton("Run Frida Script");
		runButton.setToolTipText("Execute the current Frida script");
		runButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				runFridaScript();
			}
		});
		buttonPanel.add(runButton);
		scriptPanel.add(buttonPanel, BorderLayout.SOUTH);

		splitPane.setTopComponent(scriptPanel);

		// Log panel
		JPanel logPanel = new JPanel(new BorderLayout(5, 5));
		JLabel logLabel = new JLabel("Frida Output Log:");
		logLabel.setFont(logLabel.getFont().deriveFont(Font.BOLD));
		logPanel.add(logLabel, BorderLayout.NORTH);

		logTextArea = new JTextArea(10, 80);
		logTextArea.setEditable(false);
		logTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
		JScrollPane logScrollPane = new JScrollPane(logTextArea);
		logPanel.add(logScrollPane, BorderLayout.CENTER);

		// Log button panel
		JPanel logButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		clearLogButton = new JButton("Clear Log");
		clearLogButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				logTextArea.setText("");
			}
		});
		logButtonPanel.add(clearLogButton);

		JButton clearScriptButton = new JButton("Clear Script");
		clearScriptButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				scriptTextArea.setText("");
			}
		});
		logButtonPanel.add(clearScriptButton);

		JButton gcButton = new JButton("Run Garbage Collection");
		gcButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				logTextArea.append("[INFO] Running garbage collection...\n");
				System.gc();
				logTextArea.append("[INFO] Garbage collection completed!\n");
			}
		});
		logButtonPanel.add(gcButton);

		JButton closeAllTabsButton = new JButton("Close All Tabs");
		closeAllTabsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				logTextArea.append("[INFO] Closing all tabs...\n");
				mainWindow.getTabsController().closeAllTabs();
				logTextArea.append("[INFO] All tabs closed!\n");
			}
		});
		logButtonPanel.add(closeAllTabsButton);
		logPanel.add(logButtonPanel, BorderLayout.SOUTH);

		splitPane.setBottomComponent(logPanel);
	}

	/**
	 * Generate and display a Frida hook script for the given method
	 */
	public void generateAndDisplayScript(JavaMethod method) {
		try {
			String script = jadx.frida.FridaScriptGenerator.generateMethodHook(method);
			scriptTextArea.setText(script);
			logTextArea.append("[INFO] Generated Frida script for method: " + method.getFullName() + "\n");
		} catch (Exception e) {
			LOG.error("Failed to generate Frida script", e);
			logTextArea.append("[ERROR] Failed to generate Frida script: " + e.getMessage() + "\n");
		}
	}

	/**
	 * Run the current Frida script
	 */
	private void runFridaScript() {
		String script = scriptTextArea.getText().trim();
		if (script.isEmpty()) {
			JOptionPane.showMessageDialog(this,
				"Please enter a Frida script first",
				"Error",
				JOptionPane.ERROR_MESSAGE);
			return;
		}

		// Ask for target process/package name
		String target = JOptionPane.showInputDialog(this,
			"Enter target process name or package name (e.g., com.example.app):",
			"Run Frida Script",
			JOptionPane.QUESTION_MESSAGE);

		if (target == null || target.trim().isEmpty()) {
			return;
		}

		// Run the script in a background thread
		new Thread(() -> {
			try {
				runFridaScriptInternal(target.trim(), script);
			} catch (Exception e) {
				LOG.error("Failed to run Frida script", e);
				SwingUtilities.invokeLater(() -> {
					logTextArea.append("[ERROR] " + e.getMessage() + "\n");
				});
			}
		}).start();
	}

	private void runFridaScriptInternal(String target, String script) throws IOException {
		SwingUtilities.invokeLater(() -> {
			logTextArea.append("[INFO] Starting Frida for target: " + target + "\n");
			runButton.setEnabled(false);
		});

		// Write script to temp file
		java.nio.file.Path tempScriptFile = java.nio.file.Files.createTempFile("jadx_frida_", ".js");
		java.nio.file.Files.write(tempScriptFile, script.getBytes(StandardCharsets.UTF_8));

		try {
			// Build frida command
			List<String> command = new java.util.ArrayList<>();
			command.add("frida");
			command.add("-U"); // USB device
			command.add("-f"); // Spawn
			command.add(target);
			command.add("-l"); // Load script
			command.add(tempScriptFile.toAbsolutePath().toString());
			command.add("--no-pause");

			ProcessBuilder processBuilder = new ProcessBuilder(command);
			processBuilder.redirectErrorStream(true);
			currentFridaProcess = processBuilder.start();

			// Read output
			try (BufferedReader reader = new BufferedReader(
					new InputStreamReader(currentFridaProcess.getInputStream(), StandardCharsets.UTF_8))) {
				String line;
				while ((line = reader.readLine()) != null) {
					final String logLine = line;
					SwingUtilities.invokeLater(() -> {
						logTextArea.append(logLine + "\n");
						// Auto-scroll to bottom
						logTextArea.setCaretPosition(logTextArea.getDocument().getLength());
					});
				}
			}

			int exitCode = currentFridaProcess.waitFor();
			final int finalExitCode = exitCode;
			SwingUtilities.invokeLater(() -> {
				logTextArea.append("[INFO] Frida process exited with code: " + finalExitCode + "\n");
				runButton.setEnabled(true);
			});
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IOException("Frida process interrupted", e);
		} finally {
			// Clean up temp file
			try {
				java.nio.file.Files.deleteIfExists(tempScriptFile);
			} catch (IOException e) {
				LOG.warn("Failed to delete temp script file", e);
			}
			currentFridaProcess = null;
		}
	}

	/**
	 * Stop the currently running Frida process (if any)
	 */
	public void stopFridaProcess() {
		if (currentFridaProcess != null && currentFridaProcess.isAlive()) {
			currentFridaProcess.destroy();
			SwingUtilities.invokeLater(() -> {
				logTextArea.append("[INFO] Stopped Frida process\n");
				runButton.setEnabled(true);
			});
		}
	}
}
