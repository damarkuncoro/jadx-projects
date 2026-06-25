package dexforge.engine.jadx.infrastructure;

import java.util.Objects;

import dexforge.core.ports.decompile.DecompileProgressReporter;
import dexforge.core.ports.decompile.DecompilerSession;
import jadx.api.JadxDecompiler;

public class JadxDecompilerSession implements DecompilerSession {
	private final JadxDecompiler decompiler;

	public JadxDecompilerSession(JadxDecompiler decompiler) {
		this.decompiler = Objects.requireNonNull(decompiler);
	}

	@Override
	public void load() {
		decompiler.load();
	}

	@Override
	public boolean hasClasses() {
		return !decompiler.getClasses().isEmpty();
	}

	@Override
	public boolean isSkipResources() {
		return decompiler.getArgs().isSkipResources();
	}

	@Override
	public boolean isSkipSources() {
		return decompiler.getArgs().isSkipSources();
	}

	@Override
	public void skipSources() {
		decompiler.getArgs().setSkipSources(true);
	}

	@Override
	public int getErrorsCount() {
		return decompiler.getErrorsCount();
	}

	@Override
	public void printErrorsReport() {
		decompiler.printErrorsReport();
	}

	@Override
	public void save() {
		decompiler.save();
	}

	@Override
	public void save(DecompileProgressReporter progressReporter) {
		decompiler.save(0, (done, total) -> progressReporter.onProgress(done, total));
	}

	@Override
	public <T> T unwrap(Class<T> type) {
		if (type.isInstance(decompiler)) {
			return type.cast(decompiler);
		}
		if (type.isInstance(decompiler.getArgs())) {
			return type.cast(decompiler.getArgs());
		}
		return null;
	}

	@Override
	public void close() {
		decompiler.close();
	}
}
