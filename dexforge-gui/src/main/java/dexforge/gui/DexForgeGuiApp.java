package dexforge.gui;

import com.formdev.flatlaf.FlatLightLaf;
import dexforge.gui.application.port.DecompilerPort;
import dexforge.gui.application.usecase.GetClassesUseCase;
import dexforge.gui.application.usecase.GetCodeUseCase;
import dexforge.gui.application.usecase.OpenProjectUseCase;
import dexforge.gui.infrastructure.adapter.DexForgeApiAdapter;
import dexforge.gui.presentation.view.MainWindow;
import dexforge.gui.presentation.viewmodel.MainViewModel;

/**
 * Main entry point for DexForge GUI.
 * Ties the layers together using Dependency Injection (manual for now).
 */
public final class DexForgeGuiApp {
	public static void main(String[] args) {
		setupLookAndFeel();

		// Infrastructure Layer
		DecompilerPort decompilerPort = new DexForgeApiAdapter();

		// Application Layer
		OpenProjectUseCase openProjectUseCase = new OpenProjectUseCase(decompilerPort);
		GetClassesUseCase getClassesUseCase = new GetClassesUseCase(decompilerPort);
		GetCodeUseCase getCodeUseCase = new GetCodeUseCase(decompilerPort);

		// Presentation Layer
		MainViewModel mainViewModel = new MainViewModel(openProjectUseCase, getClassesUseCase, getCodeUseCase);

		MainWindow.start(mainViewModel);
	}

	private static void setupLookAndFeel() {
		try {
			FlatLightLaf.setup();
		} catch (Exception e) {
			System.err.println("Failed to initialize LaF");
		}
	}
}
