package com.dexforge.layoutviewer.ui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

import com.dexforge.layoutviewer.model.AndroidViewNode;

public class AttributeInspectorPanel extends JPanel {
	private static final long serialVersionUID = 8717281676083533695L;

	private final DefaultTableModel model;
	private final JTable table;
	private final JLabel titleLabel;
	private final JLabel subtitleLabel;

	public AttributeInspectorPanel(AndroidViewNode root) {
		setLayout(new BorderLayout());
		JPanel header = new JPanel(new BorderLayout(4, 2));
		header.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		titleLabel = new JLabel();
		titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
		subtitleLabel = new JLabel();
		subtitleLabel.setHorizontalAlignment(SwingConstants.LEFT);
		header.add(titleLabel, BorderLayout.NORTH);
		header.add(subtitleLabel, BorderLayout.CENTER);
		add(header, BorderLayout.NORTH);

		model = new DefaultTableModel(new Object[] { "View", "Attribute", "Raw", "Resolved" }, 0);
		table = new JTable(model);
		table.setAutoCreateRowSorter(true);
		add(new JScrollPane(table), BorderLayout.CENTER);
		showNode(root);
	}

	public void showNode(AndroidViewNode node) {
		titleLabel.setText(node.getTag());
		subtitleLabel.setText(summary(node));
		model.setRowCount(0);
		appendSingleNode(node);
	}

	private void appendSingleNode(AndroidViewNode node) {
		for (Map.Entry<String, String> entry : node.getAttributes().entrySet()) {
			model.addRow(new Object[] {
					node.getTag(),
					entry.getKey(),
					entry.getValue(),
					node.getResolvedAttributes().getOrDefault(entry.getKey(), entry.getValue())
			});
		}
		if (node.getAttributes().isEmpty()) {
			model.addRow(new Object[] { node.getTag(), "", "", "" });
		}
	}

	private String summary(AndroidViewNode node) {
		String id = node.getAttribute("android:id");
		String text = node.getAttribute("android:text");
		if (text == null || text.isBlank()) {
			text = node.getAttribute("android:hint");
		}
		StringBuilder sb = new StringBuilder();
		if (id != null && !id.isBlank()) {
			sb.append(id);
		}
		if (text != null && !text.isBlank()) {
			if (sb.length() > 0) {
				sb.append("  |  ");
			}
			sb.append(text);
		}
		return sb.length() == 0 ? "No id/text attributes" : sb.toString();
	}
}
