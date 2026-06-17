package jadx.gui.frida;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
import jadx.api.ResourceFile;
import jadx.api.ResourceType;
import jadx.api.JavaClass;
import jadx.frida.*;
import jadx.gui.settings.JadxSettings;
import jadx.gui.ui.MainWindow;
import jadx.gui.ui.action.JadxAutoCompletion;
import jadx.gui.device.adb.ADBDevice;
import jadx.gui.device.adb.AdbService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

		JPanel snippetsButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
		JButton addSnippetButton = new JButton("Add Custom Snippet");
		addSnippetButton.addActionListener(this::onAddSnippetButtonClicked);
		snippetsButtonsPanel.add(addSnippetButton);

		JButton editSnippetButton = new JButton("Edit Snippet");
		editSnippetButton.addActionListener(this::onEditSnippetButtonClicked);
		snippetsButtonsPanel.add(editSnippetButton);

		JButton deleteSnippetButton = new JButton("Delete Snippet");
		deleteSnippetButton.addActionListener(this::onDeleteSnippetButtonClicked);
		snippetsButtonsPanel.add(deleteSnippetButton);

		snippetsPanel.add(snippetsButtonsPanel, BorderLayout.EAST);

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

		JButton saveAsSnippetButton = new JButton("Save as Snippet");
		saveAsSnippetButton.addActionListener(this::onSaveAsSnippetButtonClicked);
		buttonPanel.add(saveAsSnippetButton);

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

	private void onDeleteSnippetButtonClicked(ActionEvent e) {
		String selectedSnippet = (String) snippetsComboBox.getSelectedItem();
		if (selectedSnippet == null || selectedSnippet.equals(SELECT_SNIPPET_TEXT)) {
			return;
		}

		// Check if selected snippet is a default one (don't allow deleting default snippets)
		boolean isDefaultSnippet = false;
		for (FridaSnippets defaultSnippet : FridaSnippets.values()) {
			if (defaultSnippet.getDisplayName().equals(selectedSnippet)) {
				isDefaultSnippet = true;
				break;
			}
		}

		if (isDefaultSnippet) {
			JOptionPane.showMessageDialog(this,
					"Cannot delete default Frida snippets!",
					"Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		// Confirm deletion
		int confirm = JOptionPane.showConfirmDialog(this,
				"Are you sure you want to delete snippet \"" + selectedSnippet + "\"?",
				"Delete Snippet",
				JOptionPane.YES_NO_OPTION);
		if (confirm != JOptionPane.YES_OPTION) {
			return;
		}

		// Unregister snippet from registry
		snippetRegistry.unregisterSnippet(selectedSnippet);

		// Update settings
		List<CustomFridaSnippet> customSnippets = new ArrayList<>(settings.getCustomFridaSnippets());
		customSnippets.removeIf(s -> s.getName().equals(selectedSnippet));
		settings.setCustomFridaSnippets(customSnippets);

		refreshSnippetsComboBox();
		appendLog("[INFO] Deleted custom snippet: " + selectedSnippet);
	}

	private void onSaveAsSnippetButtonClicked(ActionEvent e) {
		String script = scriptTextArea.getText().trim();
		if (script.isEmpty()) {
			JOptionPane.showMessageDialog(this,
					"Script editor is empty!",
					"Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		AddSnippetDialog dialog = new AddSnippetDialog(mainWindow, script);
		dialog.setVisible(true);
		if (dialog.isConfirmed()) {
			String name = dialog.getSnippetName();
			String scriptContent = dialog.getSnippetScript();

			IFridaSnippet newSnippet = new IFridaSnippet() {
				@Override
				public String getDisplayName() {
					return name;
				}

				@Override
				public String getScript() {
					return scriptContent;
				}
			};
			snippetRegistry.registerSnippet(newSnippet);

			List<CustomFridaSnippet> customSnippets = new ArrayList<>(settings.getCustomFridaSnippets());
			customSnippets.add(new CustomFridaSnippet(name, scriptContent));
			settings.setCustomFridaSnippets(customSnippets);

			refreshSnippetsComboBox();
			appendLog("[INFO] Saved script as custom snippet: " + name);
		}
	}

	private void onEditSnippetButtonClicked(ActionEvent e) {
		String selectedSnippet = (String) snippetsComboBox.getSelectedItem();
		if (selectedSnippet == null || selectedSnippet.equals(SELECT_SNIPPET_TEXT)) {
			return;
		}

		// Check if selected snippet is a default one (don't allow editing default snippets)
		boolean isDefaultSnippet = false;
		for (FridaSnippets defaultSnippet : FridaSnippets.values()) {
			if (defaultSnippet.getDisplayName().equals(selectedSnippet)) {
				isDefaultSnippet = true;
				break;
			}
		}

		if (isDefaultSnippet) {
			JOptionPane.showMessageDialog(this,
					"Cannot edit default Frida snippets!",
					"Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		Optional<IFridaSnippet> snippetOpt = snippetRegistry.findByDisplayName(selectedSnippet);
		if (snippetOpt.isEmpty()) {
			JOptionPane.showMessageDialog(this,
					"Selected snippet not found!",
					"Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		AddSnippetDialog dialog = new AddSnippetDialog(mainWindow, selectedSnippet, snippetOpt.get().getScript());
		dialog.setVisible(true);
		if (dialog.isConfirmed()) {
			String newName = dialog.getSnippetName();
			String newScript = dialog.getSnippetScript();

			// Remove old snippet from registry
			snippetRegistry.unregisterSnippet(selectedSnippet);

			// Remove old snippet from settings
			List<CustomFridaSnippet> customSnippets = new ArrayList<>(settings.getCustomFridaSnippets());
			customSnippets.removeIf(s -> s.getName().equals(selectedSnippet));

			// Add new snippet to registry and settings
			IFridaSnippet newSnippet = new IFridaSnippet() {
				@Override
				public String getDisplayName() {
					return newName;
				}

				@Override
				public String getScript() {
					return newScript;
				}
			};
			snippetRegistry.registerSnippet(newSnippet);
			customSnippets.add(new CustomFridaSnippet(newName, newScript));

			settings.setCustomFridaSnippets(customSnippets);

			refreshSnippetsComboBox();
			appendLog("[INFO] Updated custom snippet: " + newName);
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

	private String resolveTargetPackage() {
		try {
			String pkg = mainWindow.getWrapper().getDecompiler().getRoot().getAppPackage();
			if (pkg != null && !pkg.trim().isEmpty()) {
				LOG.info("[FridaPanel] Resolved target package from root appPackage: {}", pkg);
				return pkg.trim();
			}
		} catch (Exception ex) {
			LOG.warn("[FridaPanel] Failed to get appPackage from root", ex);
		}

		try {
			List<ResourceFile> resources = mainWindow.getWrapper().getDecompiler().getResources();
			for (ResourceFile rf : resources) {
				if (rf.getType() == ResourceType.MANIFEST) {
					String content = rf.loadContent().getText().getCodeStr();
					Pattern pattern = Pattern.compile("package\\s*=\\s*\"([^\"]+)\"");
					Matcher matcher = pattern.matcher(content);
					if (matcher.find()) {
						String pkg = matcher.group(1).trim();
						LOG.info("[FridaPanel] Resolved target package from AndroidManifest: {}", pkg);
						return pkg;
					}
				}
			}
		} catch (Exception ex) {
			LOG.warn("[FridaPanel] Failed to parse AndroidManifest.xml for package name", ex);
		}

		try {
			List<JavaClass> classes = mainWindow.getWrapper().getClasses();
			if (!classes.isEmpty()) {
				String firstClassPkg = classes.get(0).getFullName();
				int lastDot = firstClassPkg.lastIndexOf('.');
				if (lastDot != -1) {
					String pkg = firstClassPkg.substring(0, lastDot);
					LOG.info("[FridaPanel] Resolved target package fallback from first class: {}", pkg);
					return pkg;
				}
			}
		} catch (Exception ex) {
			LOG.warn("[FridaPanel] Failed to get fallback package from classes", ex);
		}

		return "";
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

		String target = resolveTargetPackage();

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
		scriptTextArea.setText(script);
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

	private String getLocalFridaVersion() {
		try {
			Process process = Runtime.getRuntime().exec(new String[]{"frida", "--version"});
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
				String line = reader.readLine();
				if (line != null) {
					return line.trim();
				}
			}
		} catch (Exception e) {
			LOG.error("Failed to detect local frida version", e);
		}
		return "17.12.0"; // default fallback matching user's log
	}

	private String mapAbiToFridaArch(String abi) {
		if (abi == null) {
			return "arm64";
		}
		abi = abi.toLowerCase();
		if (abi.contains("arm64") || abi.contains("aarch64")) {
			return "arm64";
		} else if (abi.contains("arm") || abi.contains("abi")) {
			return "arm";
		} else if (abi.contains("x86_64") || abi.contains("amd64")) {
			return "x86_64";
		} else if (abi.contains("x86") || abi.contains("i386") || abi.contains("i686")) {
			return "x86";
		}
		return "arm64"; // fallback
	}

	private String downloadAndPushFridaServer(ADBDevice device) {
		try {
			String abi = AdbService.execShell(device, "getprop ro.product.cpu.abi").trim();
			String arch = mapAbiToFridaArch(abi);
			String version = getLocalFridaVersion();
			
			String binaryName = "frida-server-" + version;
			File localDir = new File(System.getProperty("user.home"), ".jadx/frida");
			if (!localDir.exists()) {
				localDir.mkdirs();
			}
			File localBinary = new File(localDir, binaryName + "-android-" + arch);
			
			if (!localBinary.exists()) {
				appendLog("[INFO] Downloading frida-server " + version + " for " + arch + "...");
				String downloadUrl = "https://github.com/frida/frida/releases/download/" + version + "/frida-server-" + version + "-android-" + arch + ".xz";
				File tempXz = File.createTempFile("frida_server_", ".xz");
				
				// Select Python 3 binary
				String pythonBin = new File("/usr/bin/python3").exists() ? "/usr/bin/python3" : "python3";
				
				String pyCmd = "import urllib.request, lzma; urllib.request.urlretrieve('" + downloadUrl + "', '" + tempXz.getAbsolutePath() + "'); " +
							   "open('" + localBinary.getAbsolutePath() + "', 'wb').write(lzma.open('" + tempXz.getAbsolutePath() + "').read())";
				
				Process process = Runtime.getRuntime().exec(new String[]{pythonBin, "-c", pyCmd});
				int exitCode = process.waitFor();
				tempXz.delete();
				
				if (exitCode != 0) {
					throw new IOException("Python download process exited with code " + exitCode);
				}
				appendLog("[INFO] Successfully downloaded frida-server binary to host: " + localBinary.getAbsolutePath());
			}

			// Push to device
			appendLog("[INFO] Pushing frida-server to /data/local/tmp/" + binaryName + "...");
			String adbPath = AdbService.detectAdbPath();
			Process pushProcess = Runtime.getRuntime().exec(new String[]{
				adbPath, "-s", device.getSerial(), "push", localBinary.getAbsolutePath(), "/data/local/tmp/" + binaryName
			});
			int pushExit = pushProcess.waitFor();
			if (pushExit != 0) {
				throw new IOException("ADB push failed with exit code " + pushExit);
			}
			appendLog("[INFO] Successfully pushed frida-server to device.");
			return binaryName;
		} catch (Exception e) {
			LOG.error("Failed to download and push frida-server", e);
			appendLog("[ERROR] Failed to download and push frida-server: " + e.getMessage());
			return null;
		}
	}

	private void downloadFridaGadget(String arch, String version) {
		try {
			File gadgetDir = new File(System.getProperty("user.home"), ".cache/frida");
			if (!gadgetDir.exists()) {
				gadgetDir.mkdirs();
			}
			File gadgetFile = new File(gadgetDir, "gadget-android-" + arch + ".so");
			if (gadgetFile.exists()) {
				return;
			}
			
			appendLog("[INFO] Downloading frida-gadget " + version + " for " + arch + "...");
			String downloadUrl = "https://github.com/frida/frida/releases/download/" + version + "/frida-gadget-" + version + "-android-" + arch + ".so.xz";
			File tempXz = File.createTempFile("frida_gadget_", ".xz");
			
			String pythonBin = new File("/usr/bin/python3").exists() ? "/usr/bin/python3" : "python3";
			
			String pyCmd = "import urllib.request, lzma; urllib.request.urlretrieve('" + downloadUrl + "', '" + tempXz.getAbsolutePath() + "'); " +
						   "open('" + gadgetFile.getAbsolutePath() + "', 'wb').write(lzma.open('" + tempXz.getAbsolutePath() + "').read())";
			
			Process process = Runtime.getRuntime().exec(new String[]{pythonBin, "-c", pyCmd});
			int exitCode = process.waitFor();
			tempXz.delete();
			
			if (exitCode != 0) {
				throw new IOException("Python download process exited with code " + exitCode);
			}
			appendLog("[INFO] Successfully downloaded frida-gadget to: " + gadgetFile.getAbsolutePath());
		} catch (Exception e) {
			LOG.error("Failed to download frida-gadget", e);
			appendLog("[ERROR] Failed to download frida-gadget: " + e.getMessage());
		}
	}

	private String findApktoolPath() {
		File optHb = new File("/opt/homebrew/bin/apktool");
		if (optHb.exists()) return optHb.getAbsolutePath();
		File usrLoc = new File("/usr/local/bin/apktool");
		if (usrLoc.exists()) return usrLoc.getAbsolutePath();
		return "apktool";
	}

	private String findBuildToolsBinary(String binaryName) {
		File buildToolsDir = new File(System.getProperty("user.home"), "Library/Android/sdk/build-tools");
		if (buildToolsDir.exists()) {
			File[] versions = buildToolsDir.listFiles(File::isDirectory);
			if (versions != null && versions.length > 0) {
				java.util.Arrays.sort(versions, (a, b) -> b.getName().compareTo(a.getName()));
				File binary = new File(versions[0], binaryName);
				if (binary.exists()) {
					return binary.getAbsolutePath();
				}
			}
		}
		return binaryName;
	}

	private String getFullyQualifiedClassName(org.w3c.dom.Document doc, String className) {
		if (className == null || className.isEmpty()) {
			return className;
		}
		if (className.contains(".") && !className.startsWith(".")) {
			return className;
		}
		String pkg = doc.getDocumentElement().getAttribute("package");
		if (pkg == null || pkg.isEmpty()) {
			return className;
		}
		if (className.startsWith(".")) {
			return pkg + className;
		}
		return pkg + "." + className;
	}

	private boolean isLauncherActivity(org.w3c.dom.Element element) {
		org.w3c.dom.NodeList intentFilters = element.getElementsByTagName("intent-filter");
		for (int j = 0; j < intentFilters.getLength(); j++) {
			org.w3c.dom.Element filter = (org.w3c.dom.Element) intentFilters.item(j);
			boolean hasMain = false;
			boolean hasLauncher = false;
			org.w3c.dom.NodeList actions = filter.getElementsByTagName("action");
			for (int k = 0; k < actions.getLength(); k++) {
				if ("android.intent.action.MAIN".equals(((org.w3c.dom.Element) actions.item(k)).getAttribute("android:name"))) {
					hasMain = true;
				}
			}
			org.w3c.dom.NodeList categories = filter.getElementsByTagName("category");
			for (int k = 0; k < categories.getLength(); k++) {
				if ("android.intent.category.LAUNCHER".equals(((org.w3c.dom.Element) categories.item(k)).getAttribute("android:name"))) {
					hasLauncher = true;
				}
			}
			if (hasMain && hasLauncher) {
				return true;
			}
		}
		return false;
	}

	private String findMainActivityFromJadx() {
		try {
			List<ResourceFile> resources = mainWindow.getWrapper().getDecompiler().getResources();
			for (ResourceFile rf : resources) {
				if (rf.getType() == ResourceType.MANIFEST) {
					String content = rf.loadContent().getText().getCodeStr();
					javax.xml.parsers.DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
					javax.xml.parsers.DocumentBuilder builder = factory.newDocumentBuilder();
					org.w3c.dom.Document doc = builder.parse(new org.xml.sax.InputSource(new java.io.StringReader(content)));
					
					// Try <activity> nodes first
					org.w3c.dom.NodeList activityNodes = doc.getElementsByTagName("activity");
					for (int i = 0; i < activityNodes.getLength(); i++) {
						org.w3c.dom.Element activity = (org.w3c.dom.Element) activityNodes.item(i);
						if (isLauncherActivity(activity)) {
							String name = activity.getAttribute("android:name");
							return getFullyQualifiedClassName(doc, name);
						}
					}
					
					// Try <activity-alias> nodes if not found in <activity>
					org.w3c.dom.NodeList aliasNodes = doc.getElementsByTagName("activity-alias");
					for (int i = 0; i < aliasNodes.getLength(); i++) {
						org.w3c.dom.Element alias = (org.w3c.dom.Element) aliasNodes.item(i);
						if (isLauncherActivity(alias)) {
							String target = alias.getAttribute("android:targetActivity");
							return getFullyQualifiedClassName(doc, target);
						}
					}
				}
			}
		} catch (Exception e) {
			LOG.error("Failed to parse JADX manifest for main activity", e);
		}
		return null;
	}

	private File findSmaliFile(File decompiledDir, String className) {
		String relativePath = className.replace('.', '/') + ".smali";
		File[] subDirs = decompiledDir.listFiles(f -> f.isDirectory() && f.getName().startsWith("smali"));
		if (subDirs != null) {
			for (File dir : subDirs) {
				File smaliFile = new File(dir, relativePath);
				if (smaliFile.exists()) {
					return smaliFile;
				}
			}
		}
		return null;
	}

	private void injectFridaGadgetLoad(File smaliFile) throws IOException {
		List<String> lines = Files.readAllLines(smaliFile.toPath(), StandardCharsets.UTF_8);
		List<String> newLines = new ArrayList<>();
		boolean inClinit = false;
		boolean injected = false;
		boolean hasClinit = false;

		for (String line : lines) {
			if (line.trim().startsWith(".method static constructor <clinit>()V")) {
				hasClinit = true;
				break;
			}
		}

		if (hasClinit) {
			for (int i = 0; i < lines.size(); i++) {
				String line = lines.get(i);
				newLines.add(line);
				if (line.trim().startsWith(".method static constructor <clinit>()V")) {
					inClinit = true;
				} else if (inClinit) {
					String trimmed = line.trim();
					if (trimmed.startsWith(".registers") || trimmed.startsWith(".locals")) {
						// Ensure we have at least 1 register/local
						String[] parts = trimmed.split("\\s+");
						if (parts.length >= 2) {
							try {
								int val = Integer.parseInt(parts[1]);
								if (val < 1) {
									newLines.remove(newLines.size() - 1);
									newLines.add(parts[0] + " 1");
								}
							} catch (NumberFormatException e) {
								// ignore, keep as is
							}
						}
						newLines.add("");
						newLines.add("    const-string v0, \"frida-gadget\"");
						newLines.add("    invoke-static {v0}, Ljava/lang/System;->loadLibrary(Ljava/lang/String;)V");
						newLines.add("");
						injected = true;
						inClinit = false;
					} else if (trimmed.startsWith(".end method") || (!trimmed.isEmpty() && !trimmed.startsWith("."))) {
						// We found an instruction or the end of the method before registers/locals declaration
						// This means they are missing. Let's inject registers and load call before this line.
						newLines.remove(newLines.size() - 1); // remove the current line
						newLines.add("    .registers 1");
						newLines.add("");
						newLines.add("    const-string v0, \"frida-gadget\"");
						newLines.add("    invoke-static {v0}, Ljava/lang/System;->loadLibrary(Ljava/lang/String;)V");
						newLines.add("");
						newLines.add(line); // re-add the current line
						injected = true;
						inClinit = false;
					}
				}
			}
		} else {
			for (String line : lines) {
				newLines.add(line);
			}
			newLines.add("");
			newLines.add(".method static constructor <clinit>()V");
			newLines.add("    .registers 1");
			newLines.add("    const-string v0, \"frida-gadget\"");
			newLines.add("    invoke-static {v0}, Ljava/lang/System;->loadLibrary(Ljava/lang/String;)V");
			newLines.add("    return-void");
			newLines.add(".end method");
			injected = true;
		}

		if (injected) {
			Files.write(smaliFile.toPath(), newLines, StandardCharsets.UTF_8);
			LOG.info("[FridaPanel] Injected frida-gadget loader into smali file: {}", smaliFile.getAbsolutePath());
		} else {
			throw new IOException("Failed to inject frida-gadget loading code into Smali");
		}
	}

	private void copyFridaGadgetLib(File decompiledDir, String arch) throws IOException {
		File libDir = new File(decompiledDir, "lib");
		if (!libDir.exists()) {
			libDir.mkdirs();
		}
		
		File[] existingDirs = libDir.listFiles(File::isDirectory);
		if (existingDirs != null && existingDirs.length > 0) {
			for (File dir : existingDirs) {
				String dirName = dir.getName();
				String targetArch = null;
				if (dirName.equals("arm64-v8a")) targetArch = "arm64";
				else if (dirName.equals("armeabi-v7a") || dirName.equals("armeabi")) targetArch = "arm";
				else if (dirName.equals("x86")) targetArch = "x86";
				else if (dirName.equals("x86_64")) targetArch = "x86_64";
				
				if (targetArch != null) {
					String version = getLocalFridaVersion();
					downloadFridaGadget(targetArch, version);
					File gadgetSrc = new File(System.getProperty("user.home"), ".cache/frida/gadget-android-" + targetArch + ".so");
					if (gadgetSrc.exists()) {
						File destFile = new File(dir, "libfrida-gadget.so");
						Files.copy(gadgetSrc.toPath(), destFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
						LOG.info("[FridaPanel] Copied frida-gadget ({}) to: {}", targetArch, destFile.getAbsolutePath());
					}
				}
			}
		} else {
			// No existing lib dirs, create one matching the connected device arch
			String abi = "arm64-v8a";
			if (arch.equals("arm")) abi = "armeabi-v7a";
			else if (arch.equals("x86")) abi = "x86";
			else if (arch.equals("x86_64")) abi = "x86_64";
			
			File dir = new File(libDir, abi);
			dir.mkdirs();
			String version = getLocalFridaVersion();
			downloadFridaGadget(arch, version);
			File gadgetSrc = new File(System.getProperty("user.home"), ".cache/frida/gadget-android-" + arch + ".so");
			if (gadgetSrc.exists()) {
				File destFile = new File(dir, "libfrida-gadget.so");
				Files.copy(gadgetSrc.toPath(), destFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
				LOG.info("[FridaPanel] Created and copied frida-gadget ({}) to: {}", arch, destFile.getAbsolutePath());
			}
		}
	}

	private void generateDebugKeystore(File keystoreFile) throws Exception {
		String keytoolPath = "keytool";
		String javaHome = System.getProperty("java.home");
		if (javaHome != null) {
			File kt = new File(javaHome, "bin/keytool");
			if (kt.exists()) {
				keytoolPath = kt.getAbsolutePath();
			}
		}
		int exitCode = runCommand(new String[]{
			keytoolPath, "-genkey", "-v", "-keystore", keystoreFile.getAbsolutePath(),
			"-alias", "jadx", "-keyalg", "RSA", "-keysize", "2048", "-validity", "10000",
			"-storepass", "android", "-keypass", "android", "-dname", "CN=JadxPatch"
		}, "keytool");
		if (exitCode != 0) {
			throw new IOException("Failed to generate debug keystore using keytool");
		}
	}

	private void signApk(File unsignedApk, File signedApk, File keystoreFile) throws Exception {
		String apksigner = findBuildToolsBinary("apksigner");
		int exitCode = runCommand(new String[]{
			apksigner, "sign", "--ks", keystoreFile.getAbsolutePath(),
			"--ks-pass", "pass:android", "--out", signedApk.getAbsolutePath(),
			unsignedApk.getAbsolutePath()
		}, "apksigner");
		if (exitCode != 0) {
			throw new IOException("Apksigner failed with exit code " + exitCode);
		}
	}

	private java.nio.file.Path findBaseApkPath(List<java.nio.file.Path> filePaths) {
		if (filePaths.size() == 1) {
			return filePaths.get(0);
		}
		// Look for exact "base.apk"
		for (java.nio.file.Path p : filePaths) {
			if (p.getFileName().toString().equalsIgnoreCase("base.apk")) {
				return p;
			}
		}
		// Look for filename containing "base"
		for (java.nio.file.Path p : filePaths) {
			if (p.getFileName().toString().toLowerCase().contains("base")) {
				return p;
			}
		}
		// Look for filename NOT containing "split"
		for (java.nio.file.Path p : filePaths) {
			if (!p.getFileName().toString().toLowerCase().contains("split")) {
				return p;
			}
		}
		// Fallback to the first one
		return filePaths.get(0);
	}

	private int runCommand(String[] command, String taskName) throws Exception {
		LOG.info("Running command for {}: {}", taskName, String.join(" ", command));
		SwingUtilities.invokeLater(() -> appendLog("[INFO] Running " + taskName + "..."));
		ProcessBuilder pb = new ProcessBuilder(command);
		pb.redirectErrorStream(true);
		Process process = pb.start();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
			String line;
			while ((line = reader.readLine()) != null) {
				String finalLine = line;
				SwingUtilities.invokeLater(() -> appendLog("[" + taskName + "] " + finalLine));
				LOG.info("[{}] {}", taskName, line);
			}
		}
		return process.waitFor();
	}

	private void autoPatchAndInstallApk(ADBDevice device, String arch, String version) {
		new Thread(() -> {
			File tempDir = null;
			List<File> signedApksToInstall = new ArrayList<>();
			try {
				appendLog("[INFO] Starting automatic APK patching process...");
				List<java.nio.file.Path> filePaths = mainWindow.getProject().getFilePaths();
				if (filePaths.isEmpty()) {
					appendLog("[ERROR] No APK file is loaded in the project.");
					return;
				}

				java.nio.file.Path baseApkPath = findBaseApkPath(filePaths);
				appendLog("[INFO] Base APK identified: " + baseApkPath.getFileName().toString());

				tempDir = Files.createTempDirectory("jadx_patch_").toFile();
				appendLog("[INFO] Decompiling base APK using apktool...");

				String apktoolPath = findApktoolPath();
				int decExit = runCommand(new String[]{
					apktoolPath, "d", "-r", "-f", "-o", tempDir.getAbsolutePath(), baseApkPath.toAbsolutePath().toString()
				}, "apktool-d");
				if (decExit != 0) {
					throw new IOException("Apktool decompilation failed with exit code " + decExit);
				}

				String mainActivity = findMainActivityFromJadx();
				if (mainActivity == null) {
					throw new IOException("MainActivity not found in AndroidManifest.xml");
				}
				appendLog("[INFO] Found MainActivity: " + mainActivity);

				File smaliFile = findSmaliFile(tempDir, mainActivity);
				if (smaliFile == null) {
					throw new IOException("MainActivity smali file not found");
				}
				injectFridaGadgetLoad(smaliFile);
				copyFridaGadgetLib(tempDir, arch);

				appendLog("[INFO] Rebuilding base APK using apktool...");
				File unsignedBaseApk = new File(tempDir.getParentFile(), "patched_base_unsigned.apk");
				int compExit = runCommand(new String[]{
					apktoolPath, "b", tempDir.getAbsolutePath(), "-o", unsignedBaseApk.getAbsolutePath()
				}, "apktool-b");
				if (compExit != 0) {
					throw new IOException("Apktool build failed with exit code " + compExit);
				}

				File keystoreFile = new File(tempDir.getParentFile(), "debug.keystore");
				if (!keystoreFile.exists()) {
					appendLog("[INFO] Generating debug keystore...");
					generateDebugKeystore(keystoreFile);
				}

				appendLog("[INFO] Signing base APK...");
				File signedBaseApk = new File(tempDir.getParentFile(), "patched_base_signed.apk");
				signApk(unsignedBaseApk, signedBaseApk, keystoreFile);
				unsignedBaseApk.delete();
				signedApksToInstall.add(signedBaseApk);

				// Process other split APKs (resign them with the same keystore)
				for (java.nio.file.Path path : filePaths) {
					if (path.equals(baseApkPath)) {
						continue;
					}
					String splitName = path.getFileName().toString();
					appendLog("[INFO] Resigning split APK: " + splitName + "...");
					File signedSplit = new File(tempDir.getParentFile(), "signed_" + splitName);
					signApk(path.toFile(), signedSplit, keystoreFile);
					signedApksToInstall.add(signedSplit);
				}

				// Uninstall the original package to avoid signature mismatch
				String adbPath = AdbService.detectAdbPath();
				String packageName = resolveTargetPackage();
				if (packageName != null && !packageName.isEmpty()) {
					appendLog("[INFO] Uninstalling existing app to prevent signature mismatch: " + packageName + "...");
					runCommand(new String[]{
						adbPath, "-s", device.getSerial(), "uninstall", packageName
					}, "adb-uninstall");
				}

				// Install all signed APKs (base + splits)
				appendLog("[INFO] Installing patched APK(s) to device " + device.getSerial() + "...");
				List<String> installCmd = new ArrayList<>();
				installCmd.add(adbPath);
				installCmd.add("-s");
				installCmd.add(device.getSerial());
				if (signedApksToInstall.size() > 1) {
					installCmd.add("install-multiple");
					installCmd.add("-r");
					for (File f : signedApksToInstall) {
						installCmd.add(f.getAbsolutePath());
					}
				} else {
					installCmd.add("install");
					installCmd.add("-r");
					installCmd.add(signedBaseApk.getAbsolutePath());
				}

				int instExit = runCommand(installCmd.toArray(new String[0]), "adb-install");
				if (instExit != 0) {
					throw new IOException("Installation failed with exit code " + instExit);
				}

				appendLog("[INFO] APK(s) successfully patched, signed, and installed!");
				appendLog("[INFO] Please open the application on your device to launch the Frida Gadget and run the script again.");
			} catch (Exception ex) {
				LOG.error("Failed to auto patch APK", ex);
				appendLog("[ERROR] Failed to auto patch APK: " + ex.getMessage());
			} finally {
				if (tempDir != null) {
					deleteDirectory(tempDir);
				}
				for (File f : signedApksToInstall) {
					if (f.exists()) {
						f.delete();
					}
				}
			}
		}).start();
	}

	private void deleteDirectory(File dir) {
		File[] files = dir.listFiles();
		if (files != null) {
			for (File f : files) {
				if (f.isDirectory()) {
					deleteDirectory(f);
				} else {
					f.delete();
				}
			}
		}
		dir.delete();
	}

	private boolean autoStartFridaServer() {
		try {
			appendLog("[INFO] Checking connected devices for running frida-server...");
			String host = settings.getAdbDialogHost();
			if (host == null || host.isEmpty()) {
				host = "localhost";
			}
			int port = 5037;
			try {
				port = Integer.parseInt(settings.getAdbDialogPort());
			} catch (Exception ex) {
				// use default
			}

			List<ADBDevice> devices = AdbService.listDevices(host, port);
			if (devices.isEmpty()) {
				appendLog("[WARN] No connected Android devices detected via ADB.");
				return false;
			}

			boolean isReady = false;
			for (ADBDevice device : devices) {
				String serial = device.getSerial();
				appendLog("[INFO] Inspecting device: " + serial);

				// Determine device architecture & local Frida version
				String abi = AdbService.execShell(device, "getprop ro.product.cpu.abi").trim();
				String arch = mapAbiToFridaArch(abi);
				String version = getLocalFridaVersion();

				// Check if the device is rooted
				boolean isRooted = false;
				try {
					String suTest = AdbService.execShell(device, "which su");
					if (suTest != null && suTest.contains("su")) {
						isRooted = true;
					}
				} catch (Exception ex) {
					// su not found or exec fails
				}

				if (!isRooted) {
					appendLog("[WARN] Device " + serial + " is not rooted. Automated frida-server startup is not supported on jailed devices.");
					
					// Setup port forwarding
					String adbPath = AdbService.detectAdbPath();
					try {
						Process forwardProc = Runtime.getRuntime().exec(new String[]{
							adbPath, "-s", serial, "forward", "tcp:27042", "tcp:27042"
						});
						forwardProc.waitFor();
						appendLog("[INFO] Forwarded Frida Gadget port (27042 -> 27042) via ADB.");
					} catch (Exception ex) {
						LOG.error("Failed to forward adb port", ex);
					}

					// Check if port 27042 is already listening on the device (meaning gadget is loaded)
					boolean isGadgetListening = false;
					try {
						String netstat = AdbService.execShell(device, "cat /proc/net/tcp");
						if (netstat != null && netstat.toLowerCase().contains("69a2")) {
							isGadgetListening = true;
						}
					} catch (Exception ex) {
						// ignore
					}

					if (isGadgetListening) {
						appendLog("[INFO] Frida Gadget is already listening on device. Bypassing patch dialog.");
						return true; // bypass prompt!
					}

					appendLog("[INFO] Preparing Frida Gadget fallback on the host...");
					downloadFridaGadget(arch, version);
					
					// Auto patch prompt
					int choice = JOptionPane.showConfirmDialog(FridaPanel.this,
							"Device is not rooted. If the app is already patched, make sure it is open on your device.\n" +
							"Would you like JADX to patch, sign, install, and run this APK with Frida Gadget?",
							"Non-Rooted Device Detected",
							JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE);
					if (choice == JOptionPane.YES_OPTION) {
						autoPatchAndInstallApk(device, arch, version);
						appendLog("[INFO] Patching APK with Frida Gadget. Please wait for install, open the app, then click 'Run Frida Script' again.");
						return false;
					}
					continue;
				}

				// Device is rooted. Check if frida-server is already running
				String pgrepResult = "";
				try {
					pgrepResult = AdbService.execShell(device, "pgrep -f frida-server");
				} catch (Exception ex) {
					// pgrep might not be available or fails if not running
				}

				if (pgrepResult != null && !pgrepResult.trim().isEmpty()) {
					appendLog("[INFO] frida-server is already running on device " + serial + " (PID: " + pgrepResult.trim() + ")");
					isReady = true;
					continue;
				}

				// Look for a frida-server binary in /data/local/tmp
				String filesList = "";
				try {
					filesList = AdbService.execShell(device, "ls /data/local/tmp");
				} catch (Exception ex) {
					appendLog("[WARN] Could not access /data/local/tmp on device " + serial);
					continue;
				}

				String fridaBinary = null;
				if (filesList != null) {
					for (String file : filesList.split("\n")) {
						file = file.trim();
						if (file.contains("frida-server")) {
							fridaBinary = file;
							break;
						}
					}
				}

				if (fridaBinary == null) {
					appendLog("[INFO] frida-server binary not found on device " + serial + ". Downloading matching binary...");
					fridaBinary = downloadAndPushFridaServer(device);
				}

				if (fridaBinary != null) {
					appendLog("[INFO] Found frida-server binary on device: /data/local/tmp/" + fridaBinary);
					appendLog("[INFO] Attempting to start frida-server as root...");
					
					try {
						AdbService.execShell(device, "su -c 'chmod 755 /data/local/tmp/" + fridaBinary + "'");
					} catch (Exception ex) {
						// ignore if already executable
					}

					try {
						AdbService.execShell(device, "su -c '/data/local/tmp/" + fridaBinary + " > /dev/null 2>&1 &'");
						Thread.sleep(1000);
						appendLog("[INFO] Started frida-server on device " + serial);
						isReady = true;
					} catch (Exception ex) {
						appendLog("[ERROR] Failed to execute frida-server launch command on device " + serial + ": " + ex.getMessage());
					}
				} else {
					appendLog("[WARN] Failed to setup frida-server binary in /data/local/tmp on device " + serial);
				}
			}
			return isReady;
		} catch (Exception e) {
			LOG.error("Failed to automatically start frida-server", e);
			appendLog("[WARN] Automated frida-server check/start encountered an error: " + e.getMessage());
			return false;
		}
	}

	private boolean isJailedDevice() {
		try {
			String host = settings.getAdbDialogHost();
			if (host == null || host.isEmpty()) {
				host = "localhost";
			}
			int port = 5037;
			try {
				port = Integer.parseInt(settings.getAdbDialogPort());
			} catch (Exception ex) {
				// use default
			}
			List<ADBDevice> devices = AdbService.listDevices(host, port);
			if (devices.isEmpty()) {
				return false;
			}
			for (ADBDevice device : devices) {
				String suTest = AdbService.execShell(device, "which su");
				if (suTest != null && suTest.contains("su")) {
					return false; // rooted
				}
				return true; // not rooted
			}
		} catch (Exception e) {
			// fallback
		}
		return false;
	}

	private void runFridaScript(String target, String script) {
		runButton.setEnabled(false);

		new Thread(() -> {
			try {
				if (!autoStartFridaServer()) {
					return;
				}
				String finalTarget = target;
				if (isJailedDevice()) {
					finalTarget = "Gadget";
				}
				processExecutor.execute(finalTarget, script, this::appendLog);
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
