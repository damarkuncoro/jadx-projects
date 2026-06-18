package com.dexforge.layoutviewer.ui;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import com.dexforge.layoutviewer.model.AndroidViewNode;
import com.dexforge.layoutviewer.parser.LayoutXmlParser;
import com.dexforge.layoutviewer.renderer.LayoutRenderer;
import com.dexforge.layoutviewer.resolver.ResourceResolver;

import jadx.api.ResourceFile;
import jadx.gui.treemodel.JResource;
import jadx.gui.ui.panel.ContentPanel;
import jadx.gui.ui.tab.TabbedPane;

public class LayoutViewerPanel extends ContentPanel {
	private static final long serialVersionUID = 4128680888570956355L;

	private final JTabbedPane tabs = new JTabbedPane(JTabbedPane.BOTTOM);

	public LayoutViewerPanel(TabbedPane panel, JResource resource) {
		super(panel, resource);
		setLayout(new BorderLayout());
		add(tabs, BorderLayout.CENTER);
		build(resource);
	}

	@Override
	public void loadSettings() {
		updateUI();
	}

	private void build(JResource resource) {
		String xml = resource.getCodeInfo().getCodeStr();
		try {
			AndroidViewNode root = new LayoutXmlParser().parse(xml);
			ResourceResolver resolver = ResourceResolver.fromResources(getAllResources());
			resolver.resolveTree(root);
			tabs.addTab("Preview", previewWorkspace(root));
			tabs.addTab("Resources", new ResourceInspectorPanel(resolver));
		} catch (Exception e) {
			tabs.addTab("Preview", errorPanel(e));
		}
		tabs.addTab("XML", sourcePanel(xml));
	}

	private JSplitPane previewWorkspace(AndroidViewNode root) {
		AttributeInspectorPanel attributePanel = new AttributeInspectorPanel(root);
		LayoutTreePanel treePanel = new LayoutTreePanel(root, attributePanel::showNode);
		JSplitPane inspectorSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, treePanel, attributePanel);
		inspectorSplit.setResizeWeight(0.55);
		inspectorSplit.setContinuousLayout(true);

		JSplitPane workspace = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				new JScrollPane(new LayoutRenderer(node -> {
					attributePanel.showNode(node);
					treePanel.selectNode(node);
				}).render(root)),
				inspectorSplit);
		workspace.setResizeWeight(0.72);
		workspace.setContinuousLayout(true);
		return workspace;
	}

	private List<ResourceFile> getAllResources() {
		return getMainWindow().getWrapper().getResources();
	}

	private JPanel errorPanel(Exception e) {
		JPanel panel = new JPanel(new BorderLayout());
		JLabel label = new JLabel("<html><b>Layout preview unavailable</b><br>" + escape(e.getMessage()) + "</html>",
				SwingConstants.CENTER);
		label.setBorder(new EmptyBorder(24, 24, 24, 24));
		panel.add(label, BorderLayout.CENTER);
		return panel;
	}

	private JScrollPane sourcePanel(String xml) {
		JTextArea textArea = new JTextArea(xml);
		textArea.setEditable(false);
		textArea.setCaretPosition(0);
		return new JScrollPane(textArea);
	}

	private static String escape(String value) {
		if (value == null) {
			return "";
		}
		return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
	}
}
