package dexforge.infrastructure.event;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import dexforge.domain.event.DomainEvent;

/**
 * Port untuk event bus dalam infrastructure layer.
 * Event bus publish domain events ke listeners secara asynchronous.
 */
public interface EventBusPort {
	/**
	 * Subscribe listener untuk event type tertentu.
	 */
	void subscribe(String eventType, DomainEventListener listener);

	/**
	 * Unsubscribe listener.
	 */
	void unsubscribe(String eventType, DomainEventListener listener);

	/**
	 * Publish single event ke semua listeners yang tertarik.
	 */
	CompletableFuture<Void> publish(DomainEvent event);

	/**
	 * Publish multiple events.
	 */
	CompletableFuture<Void> publishAll(List<DomainEvent> events);
}
