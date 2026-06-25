package dexforge.gui.presentation.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import dexforge.api.model.DexForgeClass;
import dexforge.api.model.DexForgeNode;
import dexforge.gui.presentation.viewmodel.MainViewModel;

public final class SearchPanel extends JPanel {
	private final MainViewModel viewModel;
	private final JTable resultsTable;
	private final DefaultTableModel tableModel;
	private final JTextField searchField;

	public SearchPanel(MainViewModel viewModel) {
		this.viewModel = viewModel;
		this.tableModel = new DefaultTableModel(new Object[]{"Type", "Name", "Full Name"}, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		this.resultsTable = new JTable(tableModel);
		this.searchField = new JTextField(30);

		setLayout(new BorderLayout());

		JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JButton searchBtn = new JButton("Search");
		searchBtn.addActionListener(e -> viewModel.search(searchField.getText()));
		searchField.addActionListener(e -> viewModel.search(searchField.getText()));

		toolbar.add(searchField);
		toolbar.add(searchBtn);

		add(toolbar, BorderLayout.NORTH);
		add(new JScrollPane(resultsTable), BorderLayout.CENTER);

		bindViewModel();
	}

	private void bindViewModel() {
		viewModel.onSearchLoaded(results -> {
			SwingUtilities.invokeLater(() -> {
				tableModel.setRowCount(0);
				for (DexForgeNode node : results) {
					tableModel.addRow(new Object[]{
							node.getClass().getSimpleName().replace("DexForge", ""),
							node.getName(),
							node.getFullName()
					});
				}
			});
		});

		resultsTable.getSelectionModel().addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting()) {
				int row = resultsTable.getSelectedRow();
				if (row != -1) {
					String fullName = (String) tableModel.getValueAt(row, 2);
					viewModel.requestClass(fullName);
				}
			}
		});
	}
}
