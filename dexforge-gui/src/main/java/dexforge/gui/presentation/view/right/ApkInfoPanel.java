package dexforge.gui.presentation.view.right;

import java.awt.BorderLayout;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import dexforge.api.model.DexForgeApkMetadata;
import dexforge.gui.presentation.viewmodel.MainViewModel;

public final class ApkInfoPanel extends JPanel {
    private final JTextArea infoArea;

    public ApkInfoPanel(MainViewModel viewModel) {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        infoArea = new JTextArea();
        infoArea.setEditable(false);
        infoArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        add(new JScrollPane(infoArea), BorderLayout.CENTER);

        viewModel.onProjectChanged(project -> {
            // After project is loaded, we can request APK Info
            // We'll update the viewModel to include apkInfo state
        });

        // We'll bind to a new listener in viewModel
    }

    public void updateInfo(DexForgeApkMetadata metadata) {
        if (metadata == null) return;

        SwingUtilities.invokeLater(() -> {
            StringBuilder sb = new StringBuilder();
            sb.append("Package: ").append(metadata.getPackageName()).append("\n");
            sb.append("Version: ").append(metadata.getVersionName())
              .append(" (").append(metadata.getVersionCode()).append(")\n");
            sb.append("Min SDK: ").append(metadata.getMinSdk()).append("\n");
            sb.append("Target SDK: ").append(metadata.getTargetSdk()).append("\n");

            sb.append("\nPermissions:\n");
            if (metadata.getPermissions().isEmpty()) {
                sb.append(" - None or not analyzed yet\n");
            } else {
                for (String p : metadata.getPermissions()) {
                    sb.append(" - ").append(p).append("\n");
                }
            }

            infoArea.setText(sb.toString());
            infoArea.setCaretPosition(0);
        });
    }
}
