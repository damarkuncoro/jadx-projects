package dexforge.gui.presentation.viewmodel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import dexforge.gui.application.usecase.GetClassesUseCase;
import dexforge.gui.application.usecase.GetCodeUseCase;
import dexforge.gui.application.usecase.OpenProjectUseCase;
import dexforge.gui.domain.model.GuiProject;

/**
 * ViewModel for the main window.
 * Decouples View from Application logic.
 */
public final class MainViewModel {
	private final OpenProjectUseCase openProjectUseCase;
	private final GetClassesUseCase getClassesUseCase;
	private final GetCodeUseCase getCodeUseCase;

	private GuiProject currentProject;
	private String currentCode = "";

	private final List<Consumer<GuiProject>> projectListeners = new ArrayList<>();
	private final List<Consumer<List<String>>> classesListeners = new ArrayList<>();
	private final List<Consumer<String>> codeListeners = new ArrayList<>();
	private final List<Consumer<String>> errorListeners = new ArrayList<>();

	public MainViewModel(OpenProjectUseCase openProjectUseCase, GetClassesUseCase getClassesUseCase, GetCodeUseCase getCodeUseCase) {
		this.openProjectUseCase = openProjectUseCase;
		this.getClassesUseCase = getClassesUseCase;
		this.getCodeUseCase = getCodeUseCase;
	}

	public void openFile(File file) {
		try {
			this.currentProject = openProjectUseCase.execute(file);
			notifyProjectChanged();
			loadClasses();
		} catch (Exception e) {
			notifyError("Failed to open file: " + e.getMessage());
		}
	}

	private void loadClasses() {
		List<String> classes = getClassesUseCase.execute();
		for (Consumer<List<String>> listener : classesListeners) {
			listener.accept(classes);
		}
	}

	public void selectClass(String className) {
		try {
			this.currentCode = getCodeUseCase.execute(className);
			notifyCodeChanged();
		} catch (Exception e) {
			notifyError("Failed to decompile class: " + e.getMessage());
		}
	}

	public void onProjectChanged(Consumer<GuiProject> listener) {
		projectListeners.add(listener);
	}

	public void onClassesLoaded(Consumer<List<String>> listener) {
		classesListeners.add(listener);
	}

	public void onCodeChanged(Consumer<String> listener) {
		codeListeners.add(listener);
	}

	public void onError(Consumer<String> listener) {
		errorListeners.add(listener);
	}

	private void notifyProjectChanged() {
		for (Consumer<GuiProject> listener : projectListeners) {
			listener.accept(currentProject);
		}
	}

	private void notifyCodeChanged() {
		for (Consumer<String> listener : codeListeners) {
			listener.accept(currentCode);
		}
	}

	private void notifyError(String message) {
		for (Consumer<String> listener : errorListeners) {
			listener.accept(message);
		}
	}
}
