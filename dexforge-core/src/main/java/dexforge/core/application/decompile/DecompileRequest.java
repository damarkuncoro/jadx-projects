package dexforge.core.application.decompile;

import dexforge.core.ports.decompile.DecompileProgressReporter;

public final class DecompileRequest {
	private final boolean quiet;
	private final DecompileProgressReporter progressReporter;
	private final DecompilePostLoadAction postLoadAction;

	private DecompileRequest(Builder builder) {
		this.quiet = builder.quiet;
		this.progressReporter = builder.progressReporter;
		this.postLoadAction = builder.postLoadAction;
	}

	public boolean isQuiet() {
		return quiet;
	}

	public DecompileProgressReporter getProgressReporter() {
		return progressReporter;
	}

	public DecompilePostLoadAction getPostLoadAction() {
		return postLoadAction;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private boolean quiet;
		private DecompileProgressReporter progressReporter = DecompileProgressReporter.NO_OP;
		private DecompilePostLoadAction postLoadAction = DecompilePostLoadAction.NO_OP;

		public Builder quiet(boolean quiet) {
			this.quiet = quiet;
			return this;
		}

		public Builder progressReporter(DecompileProgressReporter progressReporter) {
			this.progressReporter = progressReporter;
			return this;
		}

		public Builder postLoadAction(DecompilePostLoadAction postLoadAction) {
			this.postLoadAction = postLoadAction;
			return this;
		}

		public DecompileRequest build() {
			return new DecompileRequest(this);
		}
	}
}
