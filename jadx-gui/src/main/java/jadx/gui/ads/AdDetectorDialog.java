package jadx.gui.ads;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.List;

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
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Detected Ad Networks");
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
		JButton scanButton = new JButton("Scan for Ads");
		scanButton.addActionListener(e -> scanForAds());
		buttonPanel.add(scanButton, BorderLayout.WEST);
		JButton jumpButton = new JButton("Jump to Selected Class");
		jumpButton.addActionListener(e -> jumpToSelectedClass());
		buttonPanel.add(jumpButton, BorderLayout.CENTER);
		JButton closeButton = new JButton("Close");
		closeButton.addActionListener(e -> dispose());
		buttonPanel.add(closeButton, BorderLayout.EAST);

		panel.add(buttonPanel, BorderLayout.SOUTH);
	}

	private void scanForAds() {
		JadxDecompiler decompiler = mainWindow.getWrapper().getDecompiler();
		List<AdFinding> findings = AdDetector.detectAds(decompiler);

		DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();
		root.removeAllChildren();

		if (findings.isEmpty()) {
			root.add(new DefaultMutableTreeNode("No ad networks detected!"));
		} else {
			for (AdFinding finding : findings) {
				DefaultMutableTreeNode networkNode = new DefaultMutableTreeNode(finding.getNetwork().getName());
				root.add(networkNode);

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
				|| selectedText.startsWith("Detected Ad Networks")) {
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
}
