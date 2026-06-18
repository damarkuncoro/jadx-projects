package com.dexforge.layoutviewer.ui;

import java.awt.BorderLayout;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import com.dexforge.layoutviewer.model.AndroidViewNode;

public class LayoutTreePanel extends JPanel {
	private static final long serialVersionUID = -1222904914812078190L;

	private final JTree tree;
	private final Map<AndroidViewNode, TreePath> paths = new IdentityHashMap<>();

	public LayoutTreePanel(AndroidViewNode root) {
		this(root, null);
	}

	public LayoutTreePanel(AndroidViewNode root, Consumer<AndroidViewNode> selectionListener) {
		setLayout(new BorderLayout());
		DefaultMutableTreeNode rootTreeNode = toTreeNode(root);
		tree = new JTree(rootTreeNode);
		tree.setRootVisible(true);
		indexPaths(rootTreeNode, new TreePath(rootTreeNode));
		if (selectionListener != null) {
			tree.addTreeSelectionListener(event -> {
				Object node = tree.getLastSelectedPathComponent();
				if (node instanceof DefaultMutableTreeNode) {
					Object userObject = ((DefaultMutableTreeNode) node).getUserObject();
					if (userObject instanceof TreeItem) {
						selectionListener.accept(((TreeItem) userObject).getViewNode());
					}
				}
			});
		}
		add(new JScrollPane(tree), BorderLayout.CENTER);
	}

	public JTree getTree() {
		return tree;
	}

	public void selectNode(AndroidViewNode node) {
		TreePath path = paths.get(node);
		if (path == null) {
			return;
		}
		tree.setSelectionPath(path);
		tree.scrollPathToVisible(path);
	}

	private DefaultMutableTreeNode toTreeNode(AndroidViewNode node) {
		DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(new TreeItem(node, label(node)));
		for (AndroidViewNode child : node.getChildren()) {
			treeNode.add(toTreeNode(child));
		}
		return treeNode;
	}

	private void indexPaths(DefaultMutableTreeNode node, TreePath path) {
		Object userObject = node.getUserObject();
		if (userObject instanceof TreeItem) {
			paths.put(((TreeItem) userObject).getViewNode(), path);
		}
		for (int i = 0; i < node.getChildCount(); i++) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
			indexPaths(child, path.pathByAddingChild(child));
		}
	}

	private String label(AndroidViewNode node) {
		String id = node.getAttribute("android:id");
		if (id == null) {
			return node.getTag();
		}
		return node.getTag() + "  " + id;
	}

	private static final class TreeItem {
		private final AndroidViewNode viewNode;
		private final String label;

		private TreeItem(AndroidViewNode viewNode, String label) {
			this.viewNode = viewNode;
			this.label = label;
		}

		private AndroidViewNode getViewNode() {
			return viewNode;
		}

		@Override
		public String toString() {
			return label;
		}
	}
}
