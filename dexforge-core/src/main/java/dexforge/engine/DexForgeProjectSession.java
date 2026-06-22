package dexforge.engine;

import java.util.List;

import dexforge.domain.model.project.Project;

public interface DexForgeProjectSession extends AutoCloseable {
	Project getProject();

	int getClassesCount();

	int getResourcesCount();

	List<DexForgeClassInfo> listClasses();

	DexForgeClassDecompileResult decompileClass(String className);

	DexForgeDefinitionInfo getDefinition(String className, int position);

	DexForgeSourceLocation findDefinition(String uri, int line, int character);

	List<DexForgeSourceLocation> findReferences(String uri, int line, int character);

	List<DexForgeWorkspaceSymbol> findWorkspaceSymbols(String query, int limit);

	DexForgeHover getHover(String uri, int line, int character);

	void decompileProject(java.nio.file.Path outputPath, DexForgeProgressReporter progressReporter);

	List<DexForgeDiagnostic> getDiagnostics();

	default void unloadClasses() {
	}

	default boolean isIndexingComplete() {
		return true;
	}

	default int getIndexedSymbolsCount() {
		return 0;
	}

	default <T> T unwrap(Class<T> type) {
		throw new IllegalArgumentException("Cannot unwrap session to: " + type.getName());
	}

	@Override
	void close();
}
