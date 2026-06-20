package dexforge.core.application.decompile;

public final class SingleClassDecompileOptions {
	private final String className;
	private final String outputPath;

	public SingleClassDecompileOptions(String className, String outputPath) {
		this.className = className;
		this.outputPath = outputPath;
	}

	public String getClassName() {
		return className;
	}

	public String getOutputPath() {
		return outputPath;
	}

	public boolean isEnabled() {
		return className != null || outputPath != null;
	}
}
