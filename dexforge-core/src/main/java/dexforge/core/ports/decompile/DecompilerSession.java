package dexforge.core.ports.decompile;

public interface DecompilerSession extends AutoCloseable {
	void load();

	boolean hasClasses();

	boolean isSkipResources();

	boolean isSkipSources();

	void skipSources();

	int getErrorsCount();

	void printErrorsReport();

	void save();

	void save(DecompileProgressReporter progressReporter);

	<T> T unwrap(Class<T> type);

	@Override
	void close();
}
