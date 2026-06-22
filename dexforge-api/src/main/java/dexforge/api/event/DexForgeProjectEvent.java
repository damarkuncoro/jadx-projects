package dexforge.api.event;

import java.time.Instant;

import dexforge.api.core.DexForgeProject;

/**
 * Event related to project lifecycle.
 */
public final class DexForgeProjectEvent implements DexForgeEvent {
	public enum Type {
		LOADED,
		SAVED,
		CLOSED
	}

	private final Instant timestamp = Instant.now();
	private final DexForgeProject project;
	private final Type type;

	public DexForgeProjectEvent(DexForgeProject project, Type type) {
		this.project = project;
		this.type = type;
	}

	@Override
	public Instant getTimestamp() { return timestamp; }

	@Override
	public String getMessage() { return "Project " + type.name(); }

	public DexForgeProject getProject() { return project; }

	public Type getType() { return type; }
}
