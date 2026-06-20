package dexforge.domain.event;

import java.time.LocalDateTime;

/**
 * Base interface untuk Domain Events dalam DDD.
 * Setiap event domain harus implement interface ini.
 * Events immutable dan represent sesuatu yang terjadi di domain.
 */
public interface DomainEvent {
	/**
	 * Nama event type untuk routing dan subscription.
	 * Example: "ProjectOpened", "ClassDecompiled"
	 */
	String getEventType();

	/**
	 * Waktu ketika event terjadi.
	 */
	LocalDateTime getOccurredAt();
}
