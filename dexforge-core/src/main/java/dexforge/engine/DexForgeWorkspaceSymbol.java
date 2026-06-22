package dexforge.engine;

import java.util.Objects;

public final class DexForgeWorkspaceSymbol {
	private final String name;
	private final int kind;
	private final DexForgeSourceLocation location;
	private final String containerName;

	public DexForgeWorkspaceSymbol(String name, int kind, DexForgeSourceLocation location, String containerName) {
		this.name = Objects.requireNonNull(name, "Name cannot be null");
		this.kind = kind;
		this.location = Objects.requireNonNull(location, "Location cannot be null");
		this.containerName = containerName;
	}

	public String getName() {
		return name;
	}

	public int getKind() {
		return kind;
	}

	public DexForgeSourceLocation getLocation() {
		return location;
	}

	public String getContainerName() {
		return containerName;
	}
}
