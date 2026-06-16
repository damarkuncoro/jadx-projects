package jadx.gui.ads;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import jadx.api.JadxDecompiler;
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

		// Buttons
		JPanel buttonPanel = new JPanel(new BorderLayout(10, 10));
		JButton scanButton = new JButton("Scan for Ads");
		scanButton.addActionListener(e -> scanForAds());
		buttonPanel.add(scanButton, BorderLayout.WEST);
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
}
