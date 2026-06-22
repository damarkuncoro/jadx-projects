package dexforge.engine;

import java.util.Objects;

public final class DexForgeClassInfo {
	private final String fullName;
	private final String shortName;
	private final String alias;
	private final String packageName;

	public DexForgeClassInfo(String fullName, String shortName, String alias, String packageName) {
		this.fullName = Objects.requireNonNull(fullName, "Full name cannot be null");
		this.shortName = Objects.requireNonNull(shortName, "Short name cannot be null");
		this.alias = alias;
		this.packageName = packageName;
	}

	public String getFullName() {
		return fullName;
	}

	public String getShortName() {
		return shortName;
	}

	public String getAlias() {
		return alias;
	}

	public String getPackageName() {
		return packageName;
	}
}
