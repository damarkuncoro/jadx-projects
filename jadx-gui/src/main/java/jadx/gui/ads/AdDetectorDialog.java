package jadx.gui.ads;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import jadx.api.JadxDecompiler;
import jadx.gui.treemodel.JClass;
import jadx.gui.treemodel.JNode;
import jadx.gui.ui.MainWindow;
import jadx.gui.utils.UiUtils;

public class AdDetectorDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	private static final Pattern FRIDA_OVERLOADS_PATTERN = Pattern.compile(
			"(\\b[A-Za-z_$][\\w$]*)\\.([A-Za-z_$][\\w$]*)\\.overloads\\.forEach");

	private final MainWindow mainWindow;
	private JTree resultTree;
	private DefaultTreeModel treeModel;

	public AdDetectorDialog(MainWindow mainWindow) {
		super(mainWindow, "Ad Detector", ModalityType.APPLICATION_MODAL);
		this.mainWindow = mainWindow;
		initUI();
		UiUtils.addEscapeShortCutToDispose(this);
		SwingUtilities.invokeLater(this::scanForAds);
	}

	private void initUI() {
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setSize(new Dimension(700, 500));
		setLocationRelativeTo(mainWindow);

		JPanel panel = new JPanel(new BorderLayout(10, 10));
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel, BorderLayout.CENTER);

		// Result Tree
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Detected Trackers & Ads");
		treeModel = new DefaultTreeModel(root);
		resultTree = new JTree(treeModel);
		JScrollPane treeScroll = new JScrollPane(resultTree);
		panel.add(treeScroll, BorderLayout.CENTER);

		// Add double-click listener for tree
		resultTree.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					jumpToSelectedClass();
				}
			}
		});

		// Buttons
		JPanel buttonPanel = new JPanel(new BorderLayout(10, 10));

		JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		JButton scanButton = new JButton("Scan for Ads");
		scanButton.addActionListener(e -> scanForAds());
		leftPanel.add(scanButton);

		JButton fridaButton = new JButton("Send to Frida Panel");
		fridaButton.addActionListener(e -> generateFridaBypassScript());
		leftPanel.add(fridaButton);

		buttonPanel.add(leftPanel, BorderLayout.WEST);

		JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
		JButton jumpButton = new JButton("Jump to Selected Class");
		jumpButton.addActionListener(e -> jumpToSelectedClass());
		rightPanel.add(jumpButton);

		JButton closeButton = new JButton("Close");
		closeButton.addActionListener(e -> dispose());
		rightPanel.add(closeButton);

		buttonPanel.add(rightPanel, BorderLayout.EAST);

		panel.add(buttonPanel, BorderLayout.SOUTH);
	}

	private void scanForAds() {
		JadxDecompiler decompiler = mainWindow.getWrapper().getDecompiler();
		List<AdFinding> findings = AdDetector.detectAds(decompiler);

		DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();
		root.removeAllChildren();

		if (findings.isEmpty()) {
			root.add(new DefaultMutableTreeNode("No ad networks or trackers detected!"));
		} else {
			// Group findings by category
			java.util.Map<String, List<AdFinding>> grouped = new java.util.LinkedHashMap<>();
			for (AdFinding finding : findings) {
				String category = finding.getNetwork().getCategory();
				if (category == null || category.isEmpty()) {
					category = "Other";
				}
				grouped.computeIfAbsent(category, k -> new ArrayList<>()).add(finding);
			}

			for (java.util.Map.Entry<String, List<AdFinding>> entry : grouped.entrySet()) {
				String category = entry.getKey();
				List<AdFinding> catFindings = entry.getValue();
				DefaultMutableTreeNode categoryNode = new DefaultMutableTreeNode(category + " (" + catFindings.size() + ")");
				root.add(categoryNode);

				for (AdFinding finding : catFindings) {
					DefaultMutableTreeNode networkNode = new DefaultMutableTreeNode(finding.getNetwork().getName());
					categoryNode.add(networkNode);

					if (!finding.getFoundPackages().isEmpty()) {
						DefaultMutableTreeNode packagesNode =
								new DefaultMutableTreeNode("Packages (" + finding.getFoundPackages().size() + ")");
						for (String pkg : finding.getFoundPackages()) {
							packagesNode.add(new DefaultMutableTreeNode(pkg));
						}
						networkNode.add(packagesNode);
					}

					if (!finding.getFoundClasses().isEmpty()) {
						DefaultMutableTreeNode classesNode = new DefaultMutableTreeNode("Classes (" + finding.getFoundClasses().size() + ")");
						for (String cls : finding.getFoundClasses()) {
							classesNode.add(new DefaultMutableTreeNode(cls));
						}
						networkNode.add(classesNode);
					}
				}
			}
		}

		treeModel.reload();
		// Expand all nodes
		for (int i = 0; i < resultTree.getRowCount(); i++) {
			resultTree.expandRow(i);
		}
	}

	private void jumpToSelectedClass() {
		TreePath selectedPath = resultTree.getSelectionPath();
		if (selectedPath == null) {
			return;
		}

		DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) selectedPath.getLastPathComponent();
		Object userObject = selectedNode.getUserObject();

		if (!(userObject instanceof String)) {
			return;
		}

		String selectedText = (String) userObject;
		// Check if selected text looks like a class name (contains dots, not a header)
		if (!selectedText.contains(".")
				|| selectedText.startsWith("Packages")
				|| selectedText.startsWith("Classes")
				|| selectedText.startsWith("Detected")) {
			return;
		}

		// Try to find the class in the tree
		JClass jClass = findJClassByName(selectedText);
		if (jClass != null) {
			dispose();
			mainWindow.getTabsController().codeJump(jClass);
		} else {
			JOptionPane.showMessageDialog(this,
					"Class not found in current project: " + selectedText,
					"Class Not Found",
					JOptionPane.WARNING_MESSAGE);
		}
	}

	private JClass findJClassByName(String fullClassName) {
		return mainWindow.searchClassByName(fullClassName);
	}

	private void generateFridaBypassScript() {
		JadxDecompiler decompiler = mainWindow.getWrapper().getDecompiler();
		List<AdFinding> findings = AdDetector.detectAds(decompiler);

		if (findings.isEmpty()) {
			JOptionPane.showMessageDialog(this,
					"No ad networks or trackers detected in this project to generate bypass scripts.",
					"Information",
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		StringBuilder scriptBuilder = new StringBuilder();
		scriptBuilder.append("/*\n");
		scriptBuilder.append("  Auto-generated Ad & Tracker Blocker Script\n");
		scriptBuilder.append("  Target: ").append(mainWindow.getProject().getName()).append("\n");
		scriptBuilder.append("*/\n\n");
		scriptBuilder.append("Java.perform(function () {\n");
		scriptBuilder.append("    console.log(\"[*] Dynamic Ad & Tracker Blocker script loaded\");\n\n");
		appendFridaHelperFunctions(scriptBuilder);

		boolean hasTemplates = false;
		for (AdFinding finding : findings) {
			String template = finding.getNetwork().getFridaTemplate();
			if (template != null && !template.trim().isEmpty()) {
				scriptBuilder.append(hardenFridaTemplate(template)).append("\n\n");
				hasTemplates = true;
			}
		}

		if (!hasTemplates) {
			JOptionPane.showMessageDialog(this,
					"No Frida templates defined for the detected ad/tracker networks in this APK.",
					"Information",
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		scriptBuilder.append("});\n");

		dispose();
		mainWindow.showFridaPanelWithScript(scriptBuilder.toString());
	}

	private String hardenFridaTemplate(String template) {
		return FRIDA_OVERLOADS_PATTERN.matcher(template)
				.replaceAll("jadxGetOverloads($1, \"$2\", \"$1.$2\").forEach");
	}

	private void appendFridaHelperFunctions(StringBuilder scriptBuilder) {
		scriptBuilder.append("    function jadxGetOverloads(owner, methodName, label) {\n");
		scriptBuilder.append("        if (!owner || !owner[methodName] || !owner[methodName].overloads) {\n");
		scriptBuilder.append("            console.log(\"[-] \" + label + \" not found; skipping hook\");\n");
		scriptBuilder.append("            return [];\n");
		scriptBuilder.append("        }\n");
		scriptBuilder.append("        return owner[methodName].overloads;\n");
		scriptBuilder.append("    }\n\n");
	}
}
