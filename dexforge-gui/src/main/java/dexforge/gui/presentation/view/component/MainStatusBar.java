package dexforge.gui.presentation.view.component;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public final class MainStatusBar extends JPanel {
    private final JLabel statusLabel;
    private final JProgressBar progressBar;
    private final JLabel infoLabel;

    public MainStatusBar() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(0, 25));
        setBorder(new EmptyBorder(2, 5, 2, 5));

        statusLabel = new JLabel("Ready");
        add(statusLabel, BorderLayout.WEST);

        JPanel rightPanel = new JPanel(new BorderLayout(10, 0));
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        progressBar.setPreferredSize(new Dimension(150, 14));

        infoLabel = new JLabel("UTF-8");

        rightPanel.add(progressBar, BorderLayout.WEST);
        rightPanel.add(infoLabel, BorderLayout.EAST);

        add(rightPanel, BorderLayout.EAST);
    }

    public void setStatus(String status) {
        statusLabel.setText(status);
    }

    public void setLoading(boolean loading) {
        progressBar.setVisible(loading);
        statusLabel.setText(loading ? "Processing..." : "Ready");
    }
}
