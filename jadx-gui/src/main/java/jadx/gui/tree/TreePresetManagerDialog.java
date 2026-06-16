package jadx.gui.tree;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import jadx.core.utils.GsonUtils;
import jadx.gui.ui.MainWindow;

public class TreePresetManagerDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	private final MainWindow mainWindow;
	private final TreeExpansionService treeExpansionService;
	private final JList<TreePreset> presetList;
	private final DefaultListModel<TreePreset> listModel;

	public TreePresetManagerDialog(MainWindow mainWindow, TreeExpansionService treeExpansionService) {
		super(mainWindow, "Tree Presets", ModalityType.APPLICATION_MODAL);
		this.mainWindow = mainWindow;
		this.treeExpansionService = treeExpansionService;

		this.listModel = new DefaultListModel<>();
		this.presetList = new JList<>(listModel);
		this.presetList.setCellRenderer(new PresetCellRenderer());

		initUI();
		loadPresets();
	}

	private void initUI() {
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setSize(new Dimension(500, 400));
		setLocationRelativeTo(mainWindow);

		JPanel panel = new JPanel(new BorderLayout(10, 10));
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel, BorderLayout.CENTER);

		// Preset list with scroll
		JScrollPane scrollPane = new JScrollPane(presetList);
		panel.add(scrollPane, BorderLayout.CENTER);

		// Buttons
		JPanel buttonPanel = new JPanel(new BorderLayout(10, 10));

		JPanel leftButtons = new JPanel(new BorderLayout(10, 10));
		JButton saveButton = new JButton("Save Current State");
		saveButton.addActionListener(e -> savePreset());
		leftButtons.add(saveButton, BorderLayout.NORTH);

		JButton exportButton = new JButton("Export Presets");
		exportButton.addActionListener(e -> exportPresets());
		leftButtons.add(exportButton, BorderLayout.SOUTH);

		JPanel rightButtons = new JPanel(new BorderLayout(10, 10));
		JButton loadButton = new JButton("Load Selected");
		loadButton.addActionListener(e -> loadSelectedPreset());
		rightButtons.add(loadButton, BorderLayout.NORTH);

		JButton deleteButton = new JButton("Delete Selected");
		deleteButton.addActionListener(e -> deleteSelectedPreset());
		rightButtons.add(deleteButton, BorderLayout.CENTER);

		JButton importButton = new JButton("Import Presets");
		importButton.addActionListener(e -> importPresets());
		rightButtons.add(importButton, BorderLayout.SOUTH);

		buttonPanel.add(leftButtons, BorderLayout.WEST);
		buttonPanel.add(rightButtons, BorderLayout.EAST);

		JButton closeButton = new JButton("Close");
		closeButton.addActionListener(e -> dispose());
		buttonPanel.add(closeButton, BorderLayout.SOUTH);

		panel.add(buttonPanel, BorderLayout.SOUTH);
	}

	private void loadPresets() {
		listModel.clear();
		List<TreePreset> presets = mainWindow.getSettings().getTreePresets();
		for (TreePreset preset : presets) {
			listModel.addElement(preset);
		}
	}

	private void savePreset() {
		String name = JOptionPane.showInputDialog(this, "Enter preset name:");
		if (name == null || name.trim().isEmpty()) {
			return;
		}

		List<String> expandedPaths = treeExpansionService.save();
		TreePreset preset = new TreePreset(name, expandedPaths);

		// Check if name already exists, if yes replace
		List<TreePreset> presets = new ArrayList<>(mainWindow.getSettings().getTreePresets());
		presets.removeIf(p -> p.getName().equals(name));
		presets.add(preset);

		mainWindow.getSettings().setTreePresets(presets);
		mainWindow.getSettings().sync();

		loadPresets();
		JOptionPane.showMessageDialog(this, "Preset saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
	}

	private void loadSelectedPreset() {
		TreePreset selected = presetList.getSelectedValue();
		if (selected == null) {
			return;
		}

		treeExpansionService.load(selected.getExpandedPaths());
		JOptionPane.showMessageDialog(this, "Preset loaded successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
	}

	private void deleteSelectedPreset() {
		TreePreset selected = presetList.getSelectedValue();
		if (selected == null) {
			return;
		}

		int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete preset '" + selected.getName() + "'?",
				"Confirm Delete", JOptionPane.YES_NO_OPTION);
		if (confirm != JOptionPane.YES_OPTION) {
			return;
		}

		List<TreePreset> presets = new ArrayList<>(mainWindow.getSettings().getTreePresets());
		presets.removeIf(p -> p.getName().equals(selected.getName()));
		mainWindow.getSettings().setTreePresets(presets);
		mainWindow.getSettings().sync();

		loadPresets();
	}

	private void exportPresets() {
		JFileChooser fileChooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("JSON Files", "json");
		fileChooser.setFileFilter(filter);
		fileChooser.setSelectedFile(new File("tree-presets.json"));

		int option = fileChooser.showSaveDialog(this);
		if (option == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			try (FileWriter writer = new FileWriter(file)) {
				Gson gson = GsonUtils.buildGson();
				gson.toJson(mainWindow.getSettings().getTreePresets(), writer);
				JOptionPane.showMessageDialog(this, "Presets exported successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
			} catch (IOException e) {
				JOptionPane.showMessageDialog(this, "Error exporting presets: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void importPresets() {
		JFileChooser fileChooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("JSON Files", "json");
		fileChooser.setFileFilter(filter);

		int option = fileChooser.showOpenDialog(this);
		if (option == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			try (FileReader reader = new FileReader(file)) {
				Gson gson = GsonUtils.buildGson();
				List<TreePreset> importedPresets = gson.fromJson(reader, new TypeToken<List<TreePreset>>() {
				}.getType());

				List<TreePreset> existingPresets = new ArrayList<>(mainWindow.getSettings().getTreePresets());
				for (TreePreset imported : importedPresets) {
					existingPresets.removeIf(p -> p.getName().equals(imported.getName()));
					existingPresets.add(imported);
				}
				mainWindow.getSettings().setTreePresets(existingPresets);
				mainWindow.getSettings().sync();

				loadPresets();
				JOptionPane.showMessageDialog(this, "Presets imported successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
			} catch (IOException e) {
				JOptionPane.showMessageDialog(this, "Error importing presets: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private static class PresetCellRenderer extends DefaultListCellRenderer {
		private static final long serialVersionUID = 1L;

		@Override
		public java.awt.Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			if (value instanceof TreePreset) {
				setText(((TreePreset) value).getName());
			}
			return this;
		}
	}
}
