package dexforge.gui.presentation.view;

import java.awt.BorderLayout;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import dexforge.gui.presentation.viewmodel.MainViewModel;

/**
 * Main GUI Window.
 * SRP: Only handles UI layout and user interaction events.
 */
public final class MainWindow extends JFrame {
	private final MainViewModel viewModel;

	public MainWindow(MainViewModel viewModel) {
		this.viewModel = viewModel;
		initComponents();
		bindViewModel();
	}

	private void initComponents() {
		setTitle("DexForge GUI");
		setSize(1200, 800);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLocationRelativeTo(null);

		setJMenuBar(createMenuBar());

		ClassTreePanel classTree = new ClassTreePanel(viewModel);
		CodeAreaPanel codeArea = new CodeAreaPanel(viewModel);

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, classTree, codeArea);
		splitPane.setDividerLocation(300);

		getContentPane().add(splitPane, BorderLayout.CENTER);
	}

	private void bindViewModel() {
		viewModel.onProjectChanged(project -> {
			setTitle("DexForge GUI - " + project.getName());
		});

		viewModel.onError(message -> {
			JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
		});
	}

	private JMenuBar createMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		JMenuItem openItem = new JMenuItem("Open File...");
		openItem.addActionListener(e -> onOpenClicked());
		fileMenu.add(openItem);
		menuBar.add(fileMenu);
		return menuBar;
	}

	private void onOpenClicked() {
		JFileChooser chooser = new JFileChooser();
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			viewModel.openFile(chooser.getSelectedFile());
		}
	}

	public static void start(MainViewModel viewModel) {
		SwingUtilities.invokeLater(() -> {
			new MainWindow(viewModel).setVisible(true);
		});
	}
}
