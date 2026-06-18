package jadx.gui.ui.dialog;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.formdev.flatlaf.extras.FlatSVGIcon;

import jadx.api.JadxDecompiler;
import jadx.gui.utils.DexForgeBrand;
import jadx.gui.utils.Link;
import jadx.gui.utils.NLS;
import jadx.gui.utils.UiUtils;

public class AboutDialog extends JDialog {
	private static final long serialVersionUID = 5763493590584039096L;

	public AboutDialog() {
		initUI();
	}

	public final void initUI() {
		Icon logo = new FlatSVGIcon(DexForgeBrand.LOGO_SVG, 48, 48);

		JLabel name = new JLabel(DexForgeBrand.GUI_NAME, logo, SwingConstants.CENTER);
		name.setAlignmentX(0.5f);

		JLabel desc = new JLabel(DexForgeBrand.TAGLINE);
		desc.setAlignmentX(0.5f);

		JLabel version = new JLabel("Engine version: " + JadxDecompiler.getVersion());
		version.setAlignmentX(0.5f);

		JLabel poweredBy = new JLabel(DexForgeBrand.POWERED_BY);
		poweredBy.setAlignmentX(0.5f);

		String javaVm = System.getProperty("java.vm.name");
		String javaVer = System.getProperty("java.version");

		javaVm = javaVm == null ? "" : javaVm;

		JLabel javaVmLabel = new JLabel("Java VM: " + javaVm);
		javaVmLabel.setAlignmentX(0.5f);

		javaVer = javaVer == null ? "" : javaVer;
		JLabel javaVerLabel = new JLabel("Java version: " + javaVer);
		javaVerLabel.setAlignmentX(0.5f);

		JLabel basedOn = new JLabel("Based on skylot/jadx");
		basedOn.setAlignmentX(0.5f);

		Link dfLink =
				new Link("DexForge repo: https://github.com/damarkuncoro/jadx-projects", "https://github.com/damarkuncoro/jadx-projects");
		dfLink.setAlignmentX(0.5f);

		Link upstreamLink = new Link("Upstream JADX: https://github.com/skylot/jadx", "https://github.com/skylot/jadx");
		upstreamLink.setAlignmentX(0.5f);

		JPanel textPane = new JPanel();
		textPane.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
		textPane.setLayout(new BoxLayout(textPane, BoxLayout.PAGE_AXIS));
		textPane.add(Box.createRigidArea(new Dimension(0, 10)));
		textPane.add(name);
		textPane.add(Box.createRigidArea(new Dimension(0, 10)));
		textPane.add(desc);
		textPane.add(Box.createRigidArea(new Dimension(0, 10)));
		textPane.add(version);
		textPane.add(Box.createRigidArea(new Dimension(0, 10)));
		textPane.add(poweredBy);
		textPane.add(Box.createRigidArea(new Dimension(0, 10)));
		textPane.add(basedOn);
		textPane.add(Box.createRigidArea(new Dimension(0, 10)));
		textPane.add(dfLink);
		textPane.add(Box.createRigidArea(new Dimension(0, 10)));
		textPane.add(upstreamLink);
		textPane.add(Box.createRigidArea(new Dimension(0, 20)));
		textPane.add(javaVmLabel);
		textPane.add(javaVerLabel);
		textPane.add(Box.createRigidArea(new Dimension(0, 20)));

		JButton close = new JButton(NLS.str("tabs.close"));
		close.addActionListener(event -> dispose());
		close.setAlignmentX(0.5f);

		Container contentPane = getContentPane();
		contentPane.add(textPane, BorderLayout.CENTER);
		contentPane.add(close, BorderLayout.PAGE_END);

		UiUtils.setWindowIcons(this);

		setModalityType(ModalityType.APPLICATION_MODAL);

		setTitle(NLS.str("about_dialog.title"));
		pack();
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
	}
}
