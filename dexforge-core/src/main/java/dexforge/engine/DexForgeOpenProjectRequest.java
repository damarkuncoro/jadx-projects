package dexforge.engine;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Public request for opening a DexForge project/session.
 */
public final class DexForgeOpenProjectRequest {
	private final Path inputPath;
	private final Boolean deobfuscationOn;
	private final String commentsLevel;
	private final String decompilationMode;
	private final java.util.function.Consumer<Object> decompilerConfigurator;
	private final DexForgeProgressReporter progressReporter;

	private DexForgeOpenProjectRequest(Builder builder) {
		this.inputPath = Objects.requireNonNull(builder.inputPath, "Input path cannot be null");
		this.deobfuscationOn = builder.deobfuscationOn;
		this.commentsLevel = builder.commentsLevel;
		this.decompilationMode = builder.decompilationMode;
		this.decompilerConfigurator = builder.decompilerConfigurator;
		this.progressReporter = builder.progressReporter;
	}

	public Path getInputPath() {
		return inputPath;
	}

	public Boolean getDeobfuscationOn() {
		return deobfuscationOn;
	}

	public String getCommentsLevel() {
		return commentsLevel;
	}

	public String getDecompilationMode() {
		return decompilationMode;
	}

	public java.util.function.Consumer<Object> getDecompilerConfigurator() {
		return decompilerConfigurator;
	}

	public DexForgeProgressReporter getProgressReporter() {
		return progressReporter;
	}

	public static Builder builder(Path inputPath) {
		return new Builder(inputPath);
	}

	public static final class Builder {
		private final Path inputPath;
		private Boolean deobfuscationOn;
		private String commentsLevel;
		private String decompilationMode;
		private java.util.function.Consumer<Object> decompilerConfigurator;
		private DexForgeProgressReporter progressReporter;

		private Builder(Path inputPath) {
			this.inputPath = inputPath;
		}

		public Builder deobfuscationOn(Boolean deobfuscationOn) {
			this.deobfuscationOn = deobfuscationOn;
			return this;
		}

		public Builder commentsLevel(String commentsLevel) {
			this.commentsLevel = commentsLevel;
			return this;
		}

		public Builder decompilationMode(String decompilationMode) {
			this.decompilationMode = decompilationMode;
			return this;
		}

		public Builder decompilerConfigurator(java.util.function.Consumer<Object> decompilerConfigurator) {
			this.decompilerConfigurator = decompilerConfigurator;
			return this;
		}

		public Builder progressReporter(DexForgeProgressReporter progressReporter) {
			this.progressReporter = progressReporter;
			return this;
		}

		public DexForgeOpenProjectRequest build() {
			return new DexForgeOpenProjectRequest(this);
		}
	}
}
