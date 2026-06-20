package dexforge.infrastructure.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dexforge.application.port.EventPublisher;
import dexforge.domain.event.DomainEvent;
import dexforge.infrastructure.event.DomainEventListener;

/**
 * Infrastructure Adapter: SimpleEventPublisher
 * Implements EventPublisher port using simple in-memory event bus.
 */
public class SimpleEventPublisher implements EventPublisher {
	private final ExecutorService executor = Executors.newFixedThreadPool(2);
	private final List<DomainEventListener> listeners = new ArrayList<>();

	@Override
	public CompletableFuture<Void> publish(DomainEvent event) {
		return CompletableFuture.runAsync(() -> {
			listeners.stream()
					.filter(l -> l.supports(event.getEventType()))
					.forEach(l -> l.handle(event));
		}, executor);
	}

	@Override
	public CompletableFuture<Void> publishAll(List<DomainEvent> events) {
		return CompletableFuture.runAsync(() -> {
			events.forEach(this::publish);
		}, executor);
	}

	public void subscribe(DomainEventListener listener) {
		listeners.add(listener);
	}

	public void unsubscribe(DomainEventListener listener) {
		listeners.remove(listener);
	}
}
