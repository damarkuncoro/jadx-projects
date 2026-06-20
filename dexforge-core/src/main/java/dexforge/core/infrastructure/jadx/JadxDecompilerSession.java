package dexforge.core.infrastructure.jadx;

import dexforge.core.ports.decompile.DecompileProgressReporter;
import dexforge.core.ports.decompile.DecompilerSession;

import jadx.api.JadxArgs;
import jadx.api.JadxDecompiler;

final class JadxDecompilerSession implements DecompilerSession {
	private final JadxDecompiler decompiler;

	JadxDecompilerSession(JadxArgs args) {
		this.decompiler = new JadxDecompiler(args);
	}

	@Override
	public void load() {
		decompiler.load();
	}

	@Override
	public boolean hasClasses() {
		return !decompiler.getRoot().getClasses().isEmpty();
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
		decompiler.save(500, progressReporter::onProgress);
	}

	@Override
	public <T> T unwrap(Class<T> type) {
		if (type.isInstance(decompiler)) {
			return type.cast(decompiler);
		}
		throw new IllegalArgumentException("Unsupported decompiler session type: " + type.getName());
	}

	@Override
	public void close() {
		decompiler.close();
	}
}
