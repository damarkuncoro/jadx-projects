package dexforge.engine;

import java.util.Objects;

public final class DexForgeDefinitionInfo {
	private final String name;
	private final String fullName;
	private final String declaringClass;
	private final int definitionPosition;

	public DexForgeDefinitionInfo(String name, String fullName, String declaringClass, int definitionPosition) {
		this.name = Objects.requireNonNull(name, "Name cannot be null");
		this.fullName = Objects.requireNonNull(fullName, "Full name cannot be null");
		this.declaringClass = declaringClass;
		this.definitionPosition = definitionPosition;
	}

	public String getName() {
		return name;
	}

	public String getFullName() {
		return fullName;
	}

	public String getDeclaringClass() {
		return declaringClass;
	}

	public int getDefinitionPosition() {
		return definitionPosition;
	}
}
