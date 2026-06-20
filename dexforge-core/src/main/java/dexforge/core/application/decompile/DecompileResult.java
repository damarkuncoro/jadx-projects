package dexforge.core.application.decompile;

public final class DecompileResult {
	private final int exitCode;
	private final int errorsCount;

	private DecompileResult(int exitCode, int errorsCount) {
		this.exitCode = exitCode;
		this.errorsCount = errorsCount;
	}

	public static DecompileResult success() {
		return new DecompileResult(DecompileExitCode.SUCCESS, 0);
	}

	public static DecompileResult failed(int exitCode) {
		return new DecompileResult(exitCode, 0);
	}

	public static DecompileResult failed(int exitCode, int errorsCount) {
		return new DecompileResult(exitCode, errorsCount);
	}

	public int getExitCode() {
		return exitCode;
	}

	public int getErrorsCount() {
		return errorsCount;
	}
}
