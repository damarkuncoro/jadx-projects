package dexforge.gui.presentation.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import dexforge.api.analysis.DexForgeFinding;
import dexforge.gui.presentation.viewmodel.MainViewModel;

public class AnalysisPanel extends JPanel {
	private final MainViewModel viewModel;
	private final JTable findingsTable;
	private final DefaultTableModel tableModel;

	public AnalysisPanel(MainViewModel viewModel) {
		this.viewModel = viewModel;
		this.tableModel = new DefaultTableModel(
				new Object[]{"Severity", "Type", "Message", "Location", "Suggested Fix"}, 0
		) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		this.findingsTable = new JTable(tableModel);

		setLayout(new BorderLayout());

		JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JButton runBtn = new JButton("Run Deep Analysis");
		runBtn.addActionListener(e -> viewModel.runDeepAnalysis());
		toolbar.add(runBtn);

		add(toolbar, BorderLayout.NORTH);
		add(new JScrollPane(findingsTable), BorderLayout.CENTER);

		bindViewModel();
	}

	private void bindViewModel() {
		viewModel.onFindingsLoaded(findings -> {
			SwingUtilities.invokeLater(() -> {
				tableModel.setRowCount(0);
				for (DexForgeFinding finding : findings) {
					tableModel.addRow(new Object[]{
							finding.getSeverity(),
							finding.getType(),
							finding.getMessage(),
							finding.getLocation() != null ? finding.getLocation().getFullName() : "N/A",
							finding.getSuggestedFix() != null ? finding.getSuggestedFix() : "N/A"
					});
				}
			});
		});

		findingsTable.getSelectionModel().addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting()) {
				int row = findingsTable.getSelectedRow();
				if (row != -1) {
					String location = (String) tableModel.getValueAt(row, 3);
					if (!"N/A".equals(location)) {
						viewModel.requestClass(location);
					}
				}
			}
		});
	}
}
