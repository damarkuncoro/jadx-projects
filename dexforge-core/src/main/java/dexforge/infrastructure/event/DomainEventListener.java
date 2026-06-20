package dexforge.infrastructure.event;

import java.util.concurrent.CompletableFuture;

import dexforge.domain.event.DomainEvent;

/**
 * Interface untuk listening ke domain events.
 * Implementasi harus handle event asynchronously tanpa memblokir event bus.
 */
public interface DomainEventListener {
	/**
	 * Handle domain event.
	 *
	 * @param event Domain event yang terjadi
	 */
	CompletableFuture<Void> handle(DomainEvent event);

	/**
	 * Check apakah listener ini tertarik dengan event type tertentu.
	 */
	boolean supports(String eventType);
}
