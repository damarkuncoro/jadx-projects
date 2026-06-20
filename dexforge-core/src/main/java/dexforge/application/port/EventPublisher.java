package dexforge.application.port;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import dexforge.domain.event.DomainEvent;

/**
 * Port (Output adapter): Event Bus untuk publish domain events.
 * Infrastructure layer harus implement interface ini.
 */
public interface EventPublisher {
	/**
	 * Publish single domain event.
	 */
	CompletableFuture<Void> publish(DomainEvent event);

	/**
	 * Publish multiple domain events.
	 */
	CompletableFuture<Void> publishAll(List<DomainEvent> events);
}
