package com.dexforge.layoutviewer.ui;

import java.awt.BorderLayout;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import com.dexforge.layoutviewer.model.AndroidResource;
import com.dexforge.layoutviewer.resolver.ResourceResolver;

public class ResourceInspectorPanel extends JPanel {
	private static final long serialVersionUID = -5356905766719196612L;

	public ResourceInspectorPanel(ResourceResolver resolver) {
		setLayout(new BorderLayout());
		DefaultTableModel model = new DefaultTableModel(new Object[] { "Type", "Name", "Value" }, 0);
		for (AndroidResource resource : resolver.getResources()) {
			model.addRow(new Object[] { resource.getType(), resource.getName(), resource.getValue() });
		}
		JTable table = new JTable(model);
		TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
		table.setRowSorter(sorter);
		add(filterPanel(sorter), BorderLayout.NORTH);
		add(new JScrollPane(table), BorderLayout.CENTER);
	}

	private JPanel filterPanel(TableRowSorter<DefaultTableModel> sorter) {
		JPanel panel = new JPanel(new BorderLayout(8, 0));
		panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		panel.add(new JLabel("Filter"), BorderLayout.WEST);
		JTextField filterField = new JTextField();
		filterField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent event) {
				updateFilter();
			}

			@Override
			public void removeUpdate(DocumentEvent event) {
				updateFilter();
			}

			@Override
			public void changedUpdate(DocumentEvent event) {
				updateFilter();
			}

			private void updateFilter() {
				String text = filterField.getText();
				if (text == null || text.isBlank()) {
					sorter.setRowFilter(null);
					return;
				}
				sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(text)));
			}
		});
		panel.add(filterField, BorderLayout.CENTER);
		return panel;
	}
}
