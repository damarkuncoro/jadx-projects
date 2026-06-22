package dexforge.api.event;

import java.time.Instant;

/**
 * Base interface for all DexForge events.
 */
public interface DexForgeEvent {
	/**
	 * Timestamp when the event was created.
	 */
	Instant getTimestamp();

	/**
	 * Human-readable description of the event.
	 */
	String getMessage();
}
