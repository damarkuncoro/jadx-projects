package dexforge.engine;

import java.util.Objects;

import dexforge.core.application.decompile.DecompileApplicationService;
import dexforge.core.application.decompile.DecompileRequest;
import dexforge.core.application.decompile.DecompileResult;

/**
 * DexForge engine session facade for decompile operations.
 */
public final class DexForgeSession implements AutoCloseable {
	private final DecompileApplicationService decompileService;
	private final DexForgePostLoadActionFactory postLoadActionFactory;
	private boolean closed;

	DexForgeSession(DecompileApplicationService decompileService, DexForgePostLoadActionFactory postLoadActionFactory) {
		this.decompileService = Objects.requireNonNull(decompileService, "DecompileApplicationService cannot be null");
		this.postLoadActionFactory = Objects.requireNonNull(postLoadActionFactory, "Post-load action factory cannot be null");
	}

	public DexForgeDecompileResult decompile(DexForgeDecompileRequest request) {
		Objects.requireNonNull(request, "DexForgeDecompileRequest cannot be null");
		DecompileRequest internalRequest = DecompileRequest.builder()
				.quiet(request.isQuiet())
				.progressReporter(new ProgressReporterAdapter(request.getProgressReporter()))
				.postLoadAction(postLoadActionFactory.create(request))
				.build();
		return DexForgeDecompileResult.from(decompile(internalRequest));
	}

	public DecompileResult decompile(DecompileRequest request) {
		if (closed) {
			throw new IllegalStateException("DexForge session is closed");
		}
		return decompileService.decompile(Objects.requireNonNull(request, "DecompileRequest cannot be null"));
	}

	@Override
	public void close() {
		closed = true;
	}

	private static final class ProgressReporterAdapter implements dexforge.core.ports.decompile.DecompileProgressReporter {
		private final DexForgeProgressReporter progressReporter;

		private ProgressReporterAdapter(DexForgeProgressReporter progressReporter) {
			this.progressReporter = Objects.requireNonNull(progressReporter, "Progress reporter cannot be null");
		}

		@Override
		public void onProgress(long done, long total) {
			progressReporter.onProgress(done, total);
		}

		@Override
		public void clear() {
			progressReporter.clear();
		}
	}
}
