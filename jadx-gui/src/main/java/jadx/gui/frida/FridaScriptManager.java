package jadx.gui.frida;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jadx.api.JavaMethod;
import jadx.frida.*;
import jadx.gui.settings.JadxSettings;
import jadx.gui.ui.MainWindow;

/**
 * Manages Frida script operations and snippet management.
 */
public class FridaScriptManager {
	private static final Logger LOG = LoggerFactory.getLogger(FridaScriptManager.class);
	private static final String SELECT_SNIPPET_TEXT = "Select a snippet...";

	private final MainWindow mainWindow;
	private final JadxSettings settings;
	private final FridaSnippetRegistry snippetRegistry;
	private final IFridaScriptGenerator scriptGenerator;
	private final JComboBox<String> snippetsComboBox;
	private final JTextArea scriptTextArea;
	private final Consumer<String> logAppender;

	public FridaScriptManager(
			MainWindow mainWindow,
			JadxSettings settings,
			FridaSnippetRegistry snippetRegistry,
			IFridaScriptGenerator scriptGenerator,
			JComboBox<String> snippetsComboBox,
			JTextArea scriptTextArea,
			Consumer<String> logAppender) {
		this.mainWindow = mainWindow;
		this.settings = settings;
		this.snippetRegistry = snippetRegistry;
		this.scriptGenerator = scriptGenerator;
		this.snippetsComboBox = snippetsComboBox;
		this.scriptTextArea = scriptTextArea;
		this.logAppender = logAppender;
	}

	public void loadCustomSnippets() {
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

	public void refreshSnippetsComboBox() {
		snippetsComboBox.removeAllItems();
		snippetsComboBox.addItem(SELECT_SNIPPET_TEXT);
		for (IFridaSnippet snippet : snippetRegistry.getAllSnippets()) {
			snippetsComboBox.addItem(snippet.getDisplayName());
		}
	}

	public void onSnippetSelected(ActionEvent e) {
		String selected = (String) snippetsComboBox.getSelectedItem();
		if (selected != null && !SELECT_SNIPPET_TEXT.equals(selected)) {
			snippetRegistry.findByDisplayName(selected).ifPresent(snippet -> {
				scriptTextArea.setText(snippet.getScript());
				logAppender.accept("[INFO] Loaded snippet: " + selected);
			});
		}
	}

	public void onAddSnippetButtonClicked(ActionEvent e) {
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
			logAppender.accept("[INFO] Added custom snippet: " + name);
		}
	}

	public void onDeleteSnippetButtonClicked(ActionEvent e) {
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
			JOptionPane.showMessageDialog(mainWindow,
					"Cannot delete default Frida snippets!",
					"Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		// Confirm deletion
		int confirm = JOptionPane.showConfirmDialog(mainWindow,
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
		logAppender.accept("[INFO] Deleted custom snippet: " + selectedSnippet);
	}

	public void onSaveAsSnippetButtonClicked(ActionEvent e) {
		String script = scriptTextArea.getText().trim();
		if (script.isEmpty()) {
			JOptionPane.showMessageDialog(mainWindow,
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
			logAppender.accept("[INFO] Saved script as custom snippet: " + name);
		}
	}

	public void onEditSnippetButtonClicked(ActionEvent e) {
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
			JOptionPane.showMessageDialog(mainWindow,
					"Cannot edit default Frida snippets!",
					"Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		Optional<IFridaSnippet> snippetOpt = snippetRegistry.findByDisplayName(selectedSnippet);
		if (snippetOpt.isEmpty()) {
			JOptionPane.showMessageDialog(mainWindow,
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
			logAppender.accept("[INFO] Updated custom snippet: " + newName);
		}
	}

	public void onSaveScriptClicked(ActionEvent e) {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Save Frida Script");
		fileChooser.setFileFilter(new FileNameExtensionFilter("Frida Scripts (*.js)", "js"));
		int userSelection = fileChooser.showSaveDialog(mainWindow);

		if (userSelection == JFileChooser.APPROVE_OPTION) {
			File fileToSave = fileChooser.getSelectedFile();
			if (!fileToSave.getName().toLowerCase().endsWith(".js")) {
				fileToSave = new File(fileToSave.getAbsolutePath() + ".js");
			}

			try {
				Files.write(fileToSave.toPath(), scriptTextArea.getText().getBytes(StandardCharsets.UTF_8));
				logAppender.accept("[INFO] Saved script to: " + fileToSave.getAbsolutePath());
			} catch (IOException ex) {
				LOG.error("Failed to save script", ex);
				JOptionPane.showMessageDialog(mainWindow,
						"Failed to save script: " + ex.getMessage(),
						"Error",
						JOptionPane.ERROR_MESSAGE);
				logAppender.accept("[ERROR] Failed to save script: " + ex.getMessage());
			}
		}
	}

	public void onLoadScriptClicked(ActionEvent e) {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Load Frida Script");
		fileChooser.setFileFilter(new FileNameExtensionFilter("Frida Scripts (*.js)", "js"));
		int userSelection = fileChooser.showOpenDialog(mainWindow);

		if (userSelection == JFileChooser.APPROVE_OPTION) {
			File fileToLoad = fileChooser.getSelectedFile();

			try {
				String content = Files.readString(fileToLoad.toPath(), StandardCharsets.UTF_8);
				scriptTextArea.setText(content);
				logAppender.accept("[INFO] Loaded script from: " + fileToLoad.getAbsolutePath());
			} catch (IOException ex) {
				LOG.error("Failed to load script", ex);
				JOptionPane.showMessageDialog(mainWindow,
						"Failed to load script: " + ex.getMessage(),
						"Error",
						JOptionPane.ERROR_MESSAGE);
				logAppender.accept("[ERROR] Failed to load script: " + ex.getMessage());
			}
		}
	}

	public void setScriptText(String script) {
		scriptTextArea.setText(script);
	}

	public String getScriptText() {
		return scriptTextArea.getText().trim();
	}

	public void generateAndDisplayScript(JavaMethod method) {
		try {
			String script = scriptGenerator.generateMethodHook(method);
			scriptTextArea.setText(script);
			logAppender.accept("[INFO] Generated Frida script for method: " + method.getFullName());
		} catch (Exception e) {
			LOG.error("Failed to generate Frida script", e);
			logAppender.accept("[ERROR] Failed to generate Frida script: " + e.getMessage());
		}
	}
}
