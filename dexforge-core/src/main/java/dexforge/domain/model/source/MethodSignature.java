package dexforge.domain.model.source;

/**
 * Value Object: MethodSignature
 * Type-safe identifier untuk method signature.
 */
public final class MethodSignature {
	private final String className;
	private final String methodName;
	private final String signature;

	private MethodSignature(String className, String methodName, String signature) {
		this.className = className;
		this.methodName = methodName;
		this.signature = signature != null ? signature : "";
	}

	public static MethodSignature of(String className, String methodName, String signature) {
		if (className == null || className.isBlank()) {
			throw new IllegalArgumentException("Class name cannot be null or blank");
		}
		if (methodName == null || methodName.isBlank()) {
			throw new IllegalArgumentException("Method name cannot be null or blank");
		}
		return new MethodSignature(className, methodName, signature);
	}

	public String getClassName() {
		return className;
	}

	public String getMethodName() {
		return methodName;
	}

	public String getSignature() {
		return signature;
	}

	public String getFullName() {
		return className + "." + methodName;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		MethodSignature that = (MethodSignature) o;
		return className.equals(that.className)
				&& methodName.equals(that.methodName)
				&& signature.equals(that.signature);
	}

	@Override
	public int hashCode() {
		int result = className.hashCode();
		result = 31 * result + methodName.hashCode();
		result = 31 * result + signature.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return getFullName();
	}
}
