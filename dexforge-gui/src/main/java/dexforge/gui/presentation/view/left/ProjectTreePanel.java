package dexforge.gui.presentation.view.left;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import dexforge.api.model.DexForgeClass;
import dexforge.api.model.DexForgePackage;
import dexforge.api.resource.DexForgeResourceFile;
import dexforge.gui.presentation.viewmodel.MainViewModel;

public final class ProjectTreePanel extends JPanel {
	private final MainViewModel viewModel;
	private final JTree tree;
	private final DefaultTreeModel treeModel;
	private final DefaultMutableTreeNode rootNode;
	private final DefaultMutableTreeNode sourcesRoot;
	private final DefaultMutableTreeNode resourcesRoot;

	public ProjectTreePanel(MainViewModel viewModel) {
		this.viewModel = viewModel;
		this.rootNode = new DefaultMutableTreeNode("Project");
		this.sourcesRoot = new DefaultMutableTreeNode("Source Code");
		this.resourcesRoot = new DefaultMutableTreeNode("Resources");
		this.rootNode.add(sourcesRoot);
		this.rootNode.add(resourcesRoot);

		this.treeModel = new DefaultTreeModel(rootNode);
		this.tree = new JTree(treeModel);
		this.tree.setRootVisible(false);

		setLayout(new BorderLayout());
		add(new JScrollPane(tree), BorderLayout.CENTER);

		bindViewModel();
	}

	private void bindViewModel() {
		viewModel.onPackagesLoaded(packages -> {
			SwingUtilities.invokeLater(() -> {
				sourcesRoot.removeAllChildren();
				for (DexForgePackage pkg : packages) {
					sourcesRoot.add(createPackageNode(pkg));
				}
				treeModel.reload(sourcesRoot);
			});
		});

		viewModel.onResourcesLoaded(resources -> {
			SwingUtilities.invokeLater(() -> {
				resourcesRoot.removeAllChildren();
				for (DexForgeResourceFile res : resources) {
					resourcesRoot.add(new DefaultMutableTreeNode(res));
				}
				treeModel.reload(resourcesRoot);
			});
		});

		tree.addTreeSelectionListener(e -> {
			DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
			if (selectedNode == null) return;

			Object userObject = selectedNode.getUserObject();
			if (userObject instanceof DexForgeClass) {
				viewModel.requestClass(((DexForgeClass) userObject).getFullName());
			} else if (userObject instanceof DexForgeResourceFile) {
				viewModel.requestResource((DexForgeResourceFile) userObject);
			}
		});
	}

	private DefaultMutableTreeNode createPackageNode(DexForgePackage pkg) {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(pkg.getName());
		for (DexForgePackage sub : pkg.getSubPackages()) node.add(createPackageNode(sub));
		for (DexForgeClass cls : pkg.getClasses()) node.add(new DefaultMutableTreeNode(cls));
		return node;
	}
}
