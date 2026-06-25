package dexforge.gui.presentation.view.left;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.border.EmptyBorder;

import dexforge.api.resource.DexForgeResourceFile;
import dexforge.gui.presentation.viewmodel.MainViewModel;

/**
 * Enhanced Resources Panel with Quick Filter and Type grouping.
 */
public final class ResourcesPanel extends JPanel {
	private final MainViewModel viewModel;
	private final DefaultTableModel tableModel;
	private final JTable resourcesTable;
	private final JTextField filterField;
	private final TableRowSorter<DefaultTableModel> sorter;
	private final List<DexForgeResourceFile> resources = new ArrayList<>();

	public ResourcesPanel(MainViewModel viewModel) {
		this.viewModel = viewModel;
		this.tableModel = new DefaultTableModel(new Object[]{"Name", "Type"}, 0) {
			@Override
			public boolean isCellEditable(int row, int column) { return false; }
		};
		this.resourcesTable = new JTable(tableModel);
		this.resourcesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.resourcesTable.setShowGrid(false);
		this.resourcesTable.setIntercellSpacing(new java.awt.Dimension(0, 0));
		this.resourcesTable.setRowHeight(22);

		this.filterField = new JTextField();
		this.sorter = new TableRowSorter<>(tableModel);
		this.resourcesTable.setRowSorter(sorter);

		setLayout(new BorderLayout());
		add(createHeader(), BorderLayout.NORTH);
		add(new JScrollPane(resourcesTable), BorderLayout.CENTER);

		bindViewModel();
	}

	private JPanel createHeader() {
		JPanel header = new JPanel(new BorderLayout());
		header.setBorder(new EmptyBorder(5, 5, 5, 5));

		filterField.putClientProperty("JTextField.placeholderText", "Search resources...");
		filterField.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyReleased(java.awt.event.KeyEvent e) { applyFilter(); }
		});

		header.add(filterField, BorderLayout.CENTER);

		JButton openBtn = new JButton("Open");
		openBtn.addActionListener(e -> openSelectedResource());
		header.add(openBtn, BorderLayout.EAST);

		return header;
	}

	private void bindViewModel() {
		viewModel.onResourcesLoaded(loadedResources -> {
			SwingUtilities.invokeLater(() -> {
				resources.clear();
				resources.addAll(loadedResources);
				tableModel.setRowCount(0);
				for (DexForgeResourceFile resource : resources) {
					tableModel.addRow(new Object[]{
							resource.getOriginalName(),
							resource.getType().toString().toUpperCase()
					});
				}
			});
		});

		resourcesTable.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent event) {
				if (event.getClickCount() == 2) openSelectedResource();
			}
		});
	}

	private void applyFilter() {
		String query = filterField.getText();
		if (query == null || query.isBlank()) {
			sorter.setRowFilter(null);
		} else {
			sorter.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(query)));
		}
	}

	private void openSelectedResource() {
		int selectedRow = resourcesTable.getSelectedRow();
		if (selectedRow != -1) {
			int modelRow = resourcesTable.convertRowIndexToModel(selectedRow);
			viewModel.requestResource(resources.get(modelRow));
		}
	}
}
