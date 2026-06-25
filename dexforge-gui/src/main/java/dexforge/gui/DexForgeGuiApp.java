package dexforge.gui;

import com.formdev.flatlaf.FlatDarkLaf;
import dexforge.gui.application.port.DecompilerPort;
import dexforge.gui.application.usecase.*;
import dexforge.gui.infrastructure.adapter.DexForgeApiAdapter;
import dexforge.gui.infrastructure.log.GuiLogAppender;
import dexforge.gui.presentation.view.MainWindow;
import dexforge.gui.presentation.viewmodel.MainViewModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main entry point for DexForge GUI using Swing + FlatLaf.
 */
public final class DexForgeGuiApp {
	private static final Logger LOG = LoggerFactory.getLogger(DexForgeGuiApp.class);

	public static void main(String[] args) {
		setupLookAndFeel();

		// Infrastructure Layer
		DecompilerPort decompilerPort = new DexForgeApiAdapter();

		// Application Layer
		OpenProjectUseCase openProjectUseCase = new OpenProjectUseCase(decompilerPort);
		GetClassesUseCase getClassesUseCase = new GetClassesUseCase(decompilerPort);
		GetPackagesUseCase getPackagesUseCase = new GetPackagesUseCase(decompilerPort);
		GetResourcesUseCase getResourcesUseCase = new GetResourcesUseCase(decompilerPort);
		GetCodeUseCase getCodeUseCase = new GetCodeUseCase(decompilerPort);
		GetApkInfoUseCase getApkInfoUseCase = new GetApkInfoUseCase(decompilerPort);
		RunAnalysisUseCase runAnalysisUseCase = new RunAnalysisUseCase(decompilerPort);
		SearchUseCase searchUseCase = new SearchUseCase(decompilerPort);
		GetSmaliUseCase getSmaliUseCase = new GetSmaliUseCase(decompilerPort);
		GetProjectInsightsUseCase getProjectInsightsUseCase = new GetProjectInsightsUseCase(decompilerPort);

		// Presentation Layer (ViewModel)
		MainViewModel mainViewModel = new MainViewModel(
				openProjectUseCase,
				getPackagesUseCase,
				getResourcesUseCase,
				getCodeUseCase,
				getApkInfoUseCase,
				runAnalysisUseCase,
				searchUseCase,
				getSmaliUseCase,
				getProjectInsightsUseCase
		);

		// Initialize Log Bridge
		GuiLogAppender.setLogConsumer(mainViewModel::log);
		LOG.info("DexForge GUI starting up...");

		// Start UI
		MainWindow.start(mainViewModel);
	}

	private static void setupLookAndFeel() {
		try {
			// Using Dark Theme by default for a professional look
			FlatDarkLaf.setup();
		} catch (Exception e) {
			System.err.println("Failed to initialize FlatLaf: " + e.getMessage());
		}
	}
}
