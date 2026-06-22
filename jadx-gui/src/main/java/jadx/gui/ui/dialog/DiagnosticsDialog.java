package jadx.gui.ui.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import jadx.gui.ui.MainWindow;
import dexforge.core.analysis.ErrorDensityAnalyzer;
import dexforge.core.analysis.ErrorDensityAnalyzer.AnalysisResult;
import dexforge.engine.DexForgeDiagnostic;
import dexforge.engine.DexForgeDiagnosticCategory;
import dexforge.engine.DexForgeDiagnosticSeverity;

public class DiagnosticsDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	public DiagnosticsDialog(JFrame parent, MainWindow mainWindow) {
		super(parent, "DexForge Diagnostics", true);
		setSize(new Dimension(900, 700));
		setLocationRelativeTo(parent);

		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		JTextArea textArea = new JTextArea();
		textArea.setEditable(false);
		textArea.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12));

		StringBuilder sb = new StringBuilder();
		List<DexForgeDiagnostic> diagnostics = mainWindow.getWrapper().getDiagnostics();
		int totalClasses = mainWindow.getWrapper().getClassesCount();

		sb.append("=== DexForge Diagnostics Summary ===\n\n");
		sb.append("Total classes: ").append(totalClasses).append("\n");
		sb.append("Total diagnostics: ").append(diagnostics.size()).append("\n");

		long errorCount = diagnostics.stream().filter(d -> d.getSeverity() == DexForgeDiagnosticSeverity.ERROR).count();
		long warningCount = diagnostics.stream().filter(d -> d.getSeverity() == DexForgeDiagnosticSeverity.WARNING).count();
		sb.append("Errors: ").append(errorCount).append("\n");
		sb.append("Warnings: ").append(warningCount).append("\n\n");

		AnalysisResult analysis = ErrorDensityAnalyzer.analyze(diagnostics, totalClasses);
		sb.append("=== Decompiler Error Analysis ===\n");
		sb.append(String.format("Error density: %.2f%% (%d errors)\n", analysis.getErrorDensity() * 100, analysis.getTotalErrors()));

		if (analysis.isHighErrorDensity()) {
			sb.append("\n⚠️ HIGH ERROR DENSITY DETECTED!\n");
			sb.append("Recommended action: Increase decompiler limits\n");
			sb.append("Suggested type updates limit: ").append(analysis.getRecommendedTypeUpdatesLimit()).append("\n\n");
		}

		sb.append("=== Errors by Category ===\n");
		for (Map.Entry<DexForgeDiagnosticCategory, Long> entry : analysis.getCategoryCounts().entrySet()) {
			sb.append(String.format("  %-30s: %d%n", entry.getKey(), entry.getValue()));
		}

		sb.append("\n=== All Diagnostics ===\n\n");
		for (DexForgeDiagnostic diag : diagnostics) {
			sb.append(String.format("[%s] %s", diag.getSeverity(), diag.getMessage()));
			if (diag.getSource() != null) {
				sb.append("\n  Source: ").append(diag.getSource());
			}
			if (diag.getMethod() != null) {
				sb.append("\n  Method: ").append(diag.getMethod());
			}
			sb.append("\n\n");
		}

		textArea.setText(sb.toString());
		mainPanel.add(new JScrollPane(textArea), BorderLayout.CENTER);

		JButton exportButton = new JButton("Export to JSON");
		exportButton.addActionListener(e -> exportDiagnostics(diagnostics, parent));

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttonPanel.add(exportButton);
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);

		add(mainPanel);
	}

	private void exportDiagnostics(List<DexForgeDiagnostic> diagnostics, JFrame parent) {
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle("Export Diagnostics to JSON");
		chooser.setSelectedFile(new File("diagnostics.json"));
		int result = chooser.showSaveDialog(parent);
		if (result != JFileChooser.APPROVE_OPTION) {
			return;
		}

		File file = chooser.getSelectedFile();
		try (FileWriter writer = new FileWriter(file)) {
			writer.write("[\n");
			for (int i = 0; i < diagnostics.size(); i++) {
				writer.write("  " + diagnostics.get(i).toJson());
				if (i < diagnostics.size() - 1) {
					writer.write(",");
				}
				writer.write("\n");
			}
			writer.write("]\n");
			JOptionPane.showMessageDialog(this, "Exported to: " + file.getAbsolutePath(), "Success",
					JOptionPane.INFORMATION_MESSAGE);
		} catch (IOException ex) {
			JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}
}