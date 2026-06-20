package dexforge.core.application.decompile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dexforge.core.ports.decompile.DecompilerEngine;
import dexforge.core.ports.decompile.DecompilerSession;

public class DecompileApplicationService {
	private static final Logger LOG = LoggerFactory.getLogger(DecompileApplicationService.class);

	private final DecompilerEngine engine;

	public DecompileApplicationService(DecompilerEngine engine) {
		this.engine = engine;
	}

	public DecompileResult decompile(DecompileRequest request) {
		try (DecompilerSession session = engine.open()) {
			session.load();
			if (hasFatalLoadError(session)) {
				return DecompileResult.failed(DecompileExitCode.LOAD_FAILED);
			}
			if (request.getPostLoadAction().process(session)) {
				return finish(session);
			}
			save(session, request);
			return finish(session);
		}
	}

	private boolean hasFatalLoadError(DecompilerSession session) {
		if (!session.hasClasses()) {
			if (session.isSkipResources()) {
				LOG.error("Load failed! No classes for decompile!");
				return true;
			}
			if (!session.isSkipSources()) {
				LOG.warn("No classes to decompile; decoding resources only");
				session.skipSources();
			}
		}
		int errorsCount = session.getErrorsCount();
		if (errorsCount > 0) {
			LOG.error("Loading finished with errors! Count: {}", errorsCount);
		}
		return false;
	}

	private void save(DecompilerSession session, DecompileRequest request) {
		if (request.isQuiet()) {
			session.save();
			return;
		}
		LOG.info("processing ...");
		session.save(request.getProgressReporter());
		request.getProgressReporter().clear();
	}

	private DecompileResult finish(DecompilerSession session) {
		int errorsCount = session.getErrorsCount();
		if (errorsCount != 0) {
			session.printErrorsReport();
			LOG.error("finished with errors, count: {}", errorsCount);
			return DecompileResult.failed(DecompileExitCode.FINISHED_WITH_ERRORS, errorsCount);
		}
		LOG.info("done");
		return DecompileResult.success();
	}
}
