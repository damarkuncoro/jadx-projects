package dexforge.gui.presentation.view;

import java.awt.BorderLayout;
import javax.swing.*;

import dexforge.gui.presentation.view.action.OpenProjectAction;
import dexforge.gui.presentation.view.action.RunAnalysisAction;
import dexforge.gui.presentation.view.bottom.*;
import dexforge.gui.presentation.view.editor.ManifestPanel;
import dexforge.gui.presentation.view.editor.SmaliPanel;
import dexforge.gui.presentation.view.editor.hex.HexEditorPanel;
import dexforge.gui.presentation.view.editor.ui.VisualUiEditorPanel;
import dexforge.gui.presentation.view.left.ProjectTreePanel;
import dexforge.gui.presentation.view.left.ResourcesPanel;
import dexforge.gui.presentation.view.plugin.PluginManagerPanel;
import dexforge.gui.presentation.view.right.ApkInfoPanel;
import dexforge.gui.presentation.view.inspector.MethodInfoPanel;
import dexforge.gui.presentation.view.inspector.XrefPanel;
import dexforge.gui.presentation.view.component.SideBar;
import dexforge.gui.presentation.view.component.MainStatusBar;
import dexforge.gui.presentation.view.manager.EditorManager;
import dexforge.gui.presentation.view.manager.ToolWindowDescriptor;
import dexforge.gui.presentation.view.manager.ToolWindowManager;
import dexforge.gui.presentation.viewmodel.MainViewModel;

/**
 * REUSEABLE: Main Window for DexForge IDE.
 * Implements a 4-pane docking layout: Explorer, Editor, Inspector, Dashboard.
 */
public final class MainWindow extends JFrame {
	private final MainViewModel viewModel;
	private MainStatusBar statusBar;
	private JSplitPane mainSplit;
	private ToolWindowManager toolWindowManager;
	private EditorManager editorManager;

	public MainWindow(MainViewModel viewModel) {
		this.viewModel = viewModel;
		initComponents();
		bindViewModel();
	}

	private void initComponents() {
		setTitle("DexForge IDE");
		setSize(1400, 900);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLocationRelativeTo(null);

		JPanel rootPanel = new JPanel(new BorderLayout());
		setContentPane(rootPanel);

		SideBar leftBar = new SideBar(true);
		SideBar rightBar = new SideBar(false);
		rootPanel.add(leftBar, BorderLayout.WEST);
		rootPanel.add(rightBar, BorderLayout.EAST);

		JTabbedPane leftToolWindow = new JTabbedPane(JTabbedPane.BOTTOM);
		JTabbedPane rightToolWindow = new JTabbedPane(JTabbedPane.BOTTOM);
		JTabbedPane bottomToolWindow = new JTabbedPane();
		JTabbedPane editorTabs = new JTabbedPane();

		// --- 1. EDITOR AREA (Center) ---
		editorTabs.addTab("Java", new CodeAreaPanel(viewModel));
		editorTabs.addTab("Smali", new SmaliPanel(viewModel));
		editorTabs.addTab("XML", new ManifestPanel(viewModel));
		editorTabs.addTab("Hex", new HexEditorPanel());
		editorTabs.addTab("Visual Editor", new VisualUiEditorPanel(viewModel));

		toolWindowManager = new ToolWindowManager(leftToolWindow, rightToolWindow, bottomToolWindow, leftBar, rightBar);
		editorManager = new EditorManager(editorTabs, viewModel);
		registerToolWindows(leftToolWindow, bottomToolWindow, rightToolWindow);

		// --- 2. LAYOUT MERGING (Docking) ---
		JSplitPane editorBottomSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, editorTabs, bottomToolWindow);
		editorBottomSplit.setDividerLocation(600);
		editorBottomSplit.setResizeWeight(0.8);

		JSplitPane centerRightSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, editorBottomSplit, rightToolWindow);
		centerRightSplit.setDividerLocation(1050);
		centerRightSplit.setResizeWeight(0.7);

		mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftToolWindow, centerRightSplit);
		mainSplit.setDividerLocation(250);

		rootPanel.add(mainSplit, BorderLayout.CENTER);

		statusBar = new MainStatusBar();
		rootPanel.add(statusBar, BorderLayout.SOUTH);
		setJMenuBar(createMenuBar());
	}

	private void registerToolWindows(JTabbedPane left, JTabbedPane bottom, JTabbedPane right) {
		// Explorer (Left)
		toolWindowManager.register(new ToolWindowDescriptor("project", "Project", new ProjectTreePanel(viewModel), ToolWindowDescriptor.Anchor.LEFT));
		toolWindowManager.register(new ToolWindowDescriptor("resources", "Resources", new ResourcesPanel(viewModel), ToolWindowDescriptor.Anchor.LEFT));
		toolWindowManager.register(new ToolWindowDescriptor("plugins", "Plugins", new PluginManagerPanel(viewModel), ToolWindowDescriptor.Anchor.LEFT));

		// Dashboard (Bottom)
		toolWindowManager.register(new ToolWindowDescriptor("output", "Log", new OutputPanel(viewModel), ToolWindowDescriptor.Anchor.BOTTOM));
		toolWindowManager.register(new ToolWindowDescriptor("problems", "Problems", new ProblemsPanel(viewModel), ToolWindowDescriptor.Anchor.BOTTOM));
		toolWindowManager.register(new ToolWindowDescriptor("search", "Search", new SearchPanel(viewModel), ToolWindowDescriptor.Anchor.BOTTOM));
		toolWindowManager.register(new ToolWindowDescriptor("terminal", "Terminal", new JPanel(), ToolWindowDescriptor.Anchor.BOTTOM));

		// Inspector (Right)
		toolWindowManager.register(new ToolWindowDescriptor("xref", "XREF", new XrefPanel(viewModel), ToolWindowDescriptor.Anchor.RIGHT));
		toolWindowManager.register(new ToolWindowDescriptor("method-info", "Method Info", new MethodInfoPanel(viewModel), ToolWindowDescriptor.Anchor.RIGHT));
		toolWindowManager.register(new ToolWindowDescriptor("apk-info", "APK Info", new ApkInfoPanel(viewModel), ToolWindowDescriptor.Anchor.RIGHT));
		toolWindowManager.register(new ToolWindowDescriptor("insights", "Project Insights", new ProjectInsightPanel(viewModel), ToolWindowDescriptor.Anchor.RIGHT));
	}

	private void bindViewModel() {
		viewModel.onProjectChanged(project -> {
			SwingUtilities.invokeLater(() -> setTitle("DexForge IDE - " + project.getName()));
		});
		viewModel.onLoadingStatusChanged(loading -> {
			SwingUtilities.invokeLater(() -> statusBar.setLoading(loading));
		});
		viewModel.onError(message -> {
			SwingUtilities.invokeLater(() ->
				JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE)
			);
		});
	}

	private JMenuBar createMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		fileMenu.add(new JMenuItem(new OpenProjectAction(viewModel, this)));
		menuBar.add(fileMenu);
		JMenu analysisMenu = new JMenu("Analysis");
		analysisMenu.add(new JMenuItem(new RunAnalysisAction(viewModel)));
		menuBar.add(analysisMenu);
		return menuBar;
	}

	public static void start(MainViewModel viewModel) {
		SwingUtilities.invokeLater(() -> {
			new MainWindow(viewModel).setVisible(true);
		});
	}
}
