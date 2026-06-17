package jadx.gui.frida;

import jadx.api.JavaMethod;
import jadx.frida.*;
import jadx.gui.settings.JadxSettings;
import jadx.gui.ui.MainWindow;
import jadx.gui.ui.action.JadxAutoCompletion;
import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Main panel for Frida integration in jadx-gui.
 */
public class FridaPanel extends JPanel {
    private static final Logger LOG = LoggerFactory.getLogger(FridaPanel.class);

    private final MainWindow mainWindow;
    private final JTextArea logTextArea;
    private final FridaScriptManager scriptManager;
    private final FridaDownloader downloader;
    private final FridaApkPatcher apkPatcher;
    private final FridaServerManager serverManager;
    private final IFridaProcessExecutor processExecutor;

    public FridaPanel(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        this.logTextArea = new JTextArea(10, 80);
        this.logTextArea.setEditable(false);
        this.logTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JadxSettings settings = mainWindow.getSettings();
        FridaSnippetRegistry snippetRegistry = new FridaSnippetRegistry();
        snippetRegistry.registerDefaultSnippets();
        IFridaScriptGenerator scriptGenerator = new FridaScriptGenerator();
        JComboBox<String> snippetsComboBox = new JComboBox<>();
        RSyntaxTextArea scriptTextArea = createScriptTextArea();

        this.scriptManager = new FridaScriptManager(
                mainWindow,
                settings,
                snippetRegistry,
                scriptGenerator,
                snippetsComboBox,
                scriptTextArea,
                this::appendLog
        );

        this.downloader = new FridaDownloader(this::appendLog);
        this.apkPatcher = new FridaApkPatcher(mainWindow, this::appendLog, downloader);
        this.serverManager = new FridaServerManager(mainWindow, settings, downloader, this::appendLog);
        this.processExecutor = new FridaProcessExecutor();

        initUI(snippetsComboBox, scriptTextArea);
        scriptManager.loadCustomSnippets();
        scriptManager.refreshSnippetsComboBox();
    }

    private RSyntaxTextArea createScriptTextArea() {
        RSyntaxTextArea scriptArea = new RSyntaxTextArea(20, 80);
        scriptArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT);
        scriptArea.setCodeFoldingEnabled(true);
        scriptArea.setAntiAliasingEnabled(true);

        // Add Frida API auto-completion
        AutoCompletion ac = new JadxAutoCompletion(FridaApiCompletionProvider.create());
        ac.install(scriptArea);
        return scriptArea;
    }

    private void initUI(JComboBox<String> snippetsComboBox, RSyntaxTextArea scriptTextArea) {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.5);
        add(splitPane, BorderLayout.CENTER);

        splitPane.setTopComponent(createScriptPanel(snippetsComboBox, scriptTextArea));
        splitPane.setBottomComponent(createLogPanel());
    }

    private JPanel createScriptPanel(JComboBox<String> snippetsComboBox, RSyntaxTextArea scriptTextArea) {
        JPanel scriptPanel = new JPanel(new BorderLayout(5, 5));

        // Top panel with title and snippets dropdown
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));

        JLabel scriptLabel = new JLabel("Frida Integration");
        scriptLabel.setFont(scriptLabel.getFont().deriveFont(Font.BOLD, 16f));
        topPanel.add(scriptLabel, BorderLayout.NORTH);

        JPanel snippetsPanel = new JPanel(new BorderLayout(5, 5));
        JLabel snippetsLabel = new JLabel("Predefined Snippets:");
        snippetsPanel.add(snippetsLabel, BorderLayout.WEST);
        snippetsPanel.add(snippetsComboBox, BorderLayout.CENTER);

        JPanel snippetsButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        JButton addSnippetButton = new JButton("Add Custom Snippet");
        addSnippetButton.addActionListener(scriptManager::onAddSnippetButtonClicked);
        snippetsButtonsPanel.add(addSnippetButton);

        JButton editSnippetButton = new JButton("Edit Snippet");
        editSnippetButton.addActionListener(scriptManager::onEditSnippetButtonClicked);
        snippetsButtonsPanel.add(editSnippetButton);

        JButton deleteSnippetButton = new JButton("Delete Snippet");
        deleteSnippetButton.addActionListener(scriptManager::onDeleteSnippetButtonClicked);
        snippetsButtonsPanel.add(deleteSnippetButton);

        snippetsPanel.add(snippetsButtonsPanel, BorderLayout.EAST);
        topPanel.add(snippetsPanel, BorderLayout.SOUTH);
        scriptPanel.add(topPanel, BorderLayout.NORTH);

        // Script editor
        RTextScrollPane scriptScrollPane = new RTextScrollPane(scriptTextArea);
        scriptPanel.add(scriptScrollPane, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton runButton = new JButton("Run Frida Script");
        runButton.setToolTipText("Execute the current Frida script");
        runButton.addActionListener(this::onRunButtonClicked);
        buttonPanel.add(runButton);

        JButton clearScriptButton = new JButton("Clear Script");
        clearScriptButton.addActionListener(e -> scriptTextArea.setText(""));
        buttonPanel.add(clearScriptButton);

        JButton saveScriptButton = new JButton("Save Script");
        saveScriptButton.addActionListener(scriptManager::onSaveScriptClicked);
        buttonPanel.add(saveScriptButton);

        JButton loadScriptButton = new JButton("Load Script");
        loadScriptButton.addActionListener(scriptManager::onLoadScriptClicked);
        buttonPanel.add(loadScriptButton);

        JButton saveAsSnippetButton = new JButton("Save as Snippet");
        saveAsSnippetButton.addActionListener(scriptManager::onSaveAsSnippetButtonClicked);
        buttonPanel.add(saveAsSnippetButton);

        scriptPanel.add(buttonPanel, BorderLayout.SOUTH);

        snippetsComboBox.addActionListener(scriptManager::onSnippetSelected);

        return scriptPanel;
    }

    private JPanel createLogPanel() {
        JPanel logPanel = new JPanel(new BorderLayout(5, 5));

        JLabel logLabel = new JLabel("Frida Output Log:");
        logLabel.setFont(logLabel.getFont().deriveFont(Font.BOLD));
        logPanel.add(logLabel, BorderLayout.NORTH);

        JScrollPane logScrollPane = new JScrollPane(logTextArea);
        logPanel.add(logScrollPane, BorderLayout.CENTER);

        JPanel logButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton clearLogButton = new JButton("Clear Log");
        clearLogButton.addActionListener(e -> logTextArea.setText(""));
        logButtonPanel.add(clearLogButton);
        logPanel.add(logButtonPanel, BorderLayout.SOUTH);

        return logPanel;
    }

    private void appendLog(String message) {
        SwingUtilities.invokeLater(() -> {
            logTextArea.append(message + "\n");
            logTextArea.setCaretPosition(logTextArea.getDocument().getLength());
        });
    }

    private void onRunButtonClicked(ActionEvent e) {
        String script = scriptManager.getScriptText();
        if (script.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a Frida script first",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        String target = FridaUtils.resolveTargetPackage(mainWindow);

        if (target.isEmpty()) {
            target = JOptionPane.showInputDialog(this,
                    "Enter target process name or package name (e.g., com.example.app):",
                    "Run Frida Script",
                    JOptionPane.QUESTION_MESSAGE);
            if (target == null || target.trim().isEmpty()) {
                return;
            }
            target = target.trim();
        }

        runFridaScript(target, script);
    }

    public void setScriptText(String script) {
        scriptManager.setScriptText(script);
    }

    public void generateAndDisplayScript(JavaMethod method) {
        scriptManager.generateAndDisplayScript(method);
    }

    private void runFridaScript(String target, String script) {
        try {
            // First check if we can auto-start frida-server
            serverManager.autoStartFridaServer();

            appendLog("[INFO] Running Frida script on target: " + target);
            processExecutor.execute(target, script, new IFridaProcessExecutor.LogListener() {
                @Override
                public void onLog(String line) {
                    appendLog(line);
                }
            });
        } catch (Exception ex) {
            LOG.error("Failed to run Frida script", ex);
            appendLog("[ERROR] Failed to run Frida script: " + ex.getMessage());
        }
    }
}
