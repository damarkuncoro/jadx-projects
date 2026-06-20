package dexforge.engine;

import java.util.Objects;

/**
 * Public DexForge decompile request.
 */
public final class DexForgeDecompileRequest {
	private final boolean quiet;
	private final DexForgeProgressReporter progressReporter;
	private final String singleClassName;
	private final String singleClassOutputPath;

	private DexForgeDecompileRequest(Builder builder) {
		this.quiet = builder.quiet;
		this.progressReporter = Objects.requireNonNull(builder.progressReporter, "Progress reporter cannot be null");
		this.singleClassName = builder.singleClassName;
		this.singleClassOutputPath = builder.singleClassOutputPath;
	}

	public boolean isQuiet() {
		return quiet;
	}

	public DexForgeProgressReporter getProgressReporter() {
		return progressReporter;
	}

	public String getSingleClassName() {
		return singleClassName;
	}

	public String getSingleClassOutputPath() {
		return singleClassOutputPath;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private boolean quiet;
		private DexForgeProgressReporter progressReporter = DexForgeProgressReporter.NO_OP;
		private String singleClassName;
		private String singleClassOutputPath;

		public Builder quiet(boolean quiet) {
			this.quiet = quiet;
			return this;
		}

		public Builder progressReporter(DexForgeProgressReporter progressReporter) {
			this.progressReporter = progressReporter;
			return this;
		}

		public Builder singleClass(String className, String outputPath) {
			this.singleClassName = className;
			this.singleClassOutputPath = outputPath;
			return this;
		}

		public DexForgeDecompileRequest build() {
			return new DexForgeDecompileRequest(this);
		}
	}
}
