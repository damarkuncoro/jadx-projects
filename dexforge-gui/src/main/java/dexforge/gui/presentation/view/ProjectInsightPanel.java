package dexforge.gui.presentation.view;

import java.awt.*;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import dexforge.gui.presentation.viewmodel.MainViewModel;

/**
 * Enhanced, professional dashboard for APK Intelligence.
 * Provides actionable insights in a clean, organized UI.
 */
public final class ProjectInsightPanel extends JPanel {
    private final MainViewModel viewModel;
    private final JPanel contentPanel;

    public ProjectInsightPanel(MainViewModel viewModel) {
        this.viewModel = viewModel;
        setLayout(new BorderLayout());

        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);

        bindViewModel();
    }

    private void bindViewModel() {
        viewModel.onInsightsLoaded(this::updateInsights);
    }

    private void updateInsights(Map<String, Object> insights) {
        SwingUtilities.invokeLater(() -> {
            contentPanel.removeAll();

            // 1. Header Section
            addSectionHeader("Project Overview");
            addInfoRow("Framework", String.valueOf(insights.get("framework")));
            addInfoRow("Security Score", String.valueOf(insights.get("securityScore")));
            addInfoRow("Recovery Status", String.valueOf(insights.get("deobfuscationStats")));

            // 2. Intelligence Section
            addSectionHeader("Detected Technologies");
            List<?> techs = (List<?>) insights.get("technologies");
            if (techs != null) {
                techs.forEach(t -> addBulletPoint(String.valueOf(t)));
            }

            // 3. Hot Methods Section
            addSectionHeader("Hot Execution Paths (Highest Fan-in)");
            List<?> hotMethods = (List<?>) insights.get("hotMethods");
            if (hotMethods != null) {
                hotMethods.forEach(m -> addBulletPoint(String.valueOf(m)));
            }

            // 4. Critical Findings Section
            addSectionHeader("Critical Findings");
            List<?> leaks = (List<?>) insights.get("piiLeaks");
            if (leaks != null && !leaks.isEmpty()) {
                leaks.forEach(l -> addAlertRow(String.valueOf(l)));
            } else {
                addBulletPoint("No critical PII leaks detected.");
            }

            contentPanel.revalidate();
            contentPanel.repaint();
        });
    }

    private void addSectionHeader(String title) {
        JLabel header = new JLabel(title);
        header.setFont(new Font("SansSerif", Font.BOLD, 14));
        header.setForeground(new Color(60, 63, 65));
        header.setBorder(new EmptyBorder(15, 10, 5, 10));
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(header);
    }

    private void addInfoRow(String label, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        row.setBackground(Color.WHITE);
        row.setBorder(new EmptyBorder(2, 20, 2, 10));

        JLabel lbl = new JLabel(label + ":");
        lbl.setForeground(Color.GRAY);
        JLabel val = new JLabel(value);
        val.setFont(new Font("SansSerif", Font.PLAIN, 12));

        row.add(lbl, BorderLayout.WEST);
        row.add(val, BorderLayout.CENTER);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(row);
    }

    private void addBulletPoint(String text) {
        JLabel bullet = new JLabel("• " + text);
        bullet.setFont(new Font("Monospaced", Font.PLAIN, 11));
        bullet.setBorder(new EmptyBorder(2, 25, 2, 10));
        bullet.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(bullet);
    }

    private void addAlertRow(String text) {
        JLabel alert = new JLabel("⚠️ " + text);
        alert.setForeground(new Color(191, 54, 12));
        alert.setFont(new Font("SansSerif", Font.BOLD, 12));
        alert.setBorder(new EmptyBorder(5, 20, 5, 10));
        alert.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(alert);
    }
}
