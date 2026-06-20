package dexforge.infrastructure.event;

import java.util.*;
import java.util.concurrent.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dexforge.domain.event.DomainEvent;

/**
 * Implementasi Event Bus menggunakan simple pub-sub pattern.
 * Events di-deliver asynchronously ke listeners menggunakan thread pool.
 *
 * Thread-safe dan dapat digunakan dari multiple threads.
 */
public class SimpleEventBus implements EventBusPort {
	private static final Logger LOG = LoggerFactory.getLogger(SimpleEventBus.class);

	private final Map<String, List<DomainEventListener>> subscribers = new ConcurrentHashMap<>();
	private final ExecutorService executorService;
	private final boolean ownedExecutor;

	/**
	 * Constructor dengan default thread pool (4 threads).
	 */
	public SimpleEventBus() {
		this(Executors.newFixedThreadPool(4, r -> {
			Thread t = new Thread(r, "DexForge-EventBus-" + System.nanoTime());
			t.setDaemon(false);
			return t;
		}), true);
	}

	/**
	 * Constructor dengan custom executor service.
	 */
	public SimpleEventBus(ExecutorService executorService) {
		this(executorService, false);
	}

	/**
	 * Private constructor yang menghandle initialization.
	 */
	private SimpleEventBus(ExecutorService executorService, boolean ownedExecutor) {
		this.executorService = executorService;
		this.ownedExecutor = ownedExecutor;
	}

	@Override
	public void subscribe(String eventType, DomainEventListener listener) {
		Objects.requireNonNull(eventType, "Event type cannot be null");
		Objects.requireNonNull(listener, "Listener cannot be null");

		subscribers.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>())
				.add(listener);

		LOG.debug("Subscriber added for event: {} (total: {})",
				eventType, subscribers.get(eventType).size());
	}

	@Override
	public void unsubscribe(String eventType, DomainEventListener listener) {
		Objects.requireNonNull(eventType, "Event type cannot be null");
		Objects.requireNonNull(listener, "Listener cannot be null");

		List<DomainEventListener> listeners = subscribers.get(eventType);
		if (listeners != null) {
			listeners.remove(listener);
			LOG.debug("Subscriber removed for event: {} (total: {})",
					eventType, listeners.size());
		}
	}

	@Override
	public CompletableFuture<Void> publish(DomainEvent event) {
		Objects.requireNonNull(event, "Event cannot be null");

		String eventType = event.getEventType();
		List<DomainEventListener> listeners = subscribers.getOrDefault(eventType, Collections.emptyList());

		LOG.info("Publishing event: {} to {} subscribers", eventType, listeners.size());

		List<CompletableFuture<Void>> futures = new ArrayList<>();

		for (DomainEventListener listener : listeners) {
			CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
				try {
					listener.handle(event).join();
				} catch (Exception e) {
					LOG.error("Error handling event: {} in listener: {}",
							eventType, listener.getClass().getSimpleName(), e);
				}
			}, executorService);

			futures.add(future);
		}

		return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
				.exceptionally(throwable -> {
					LOG.error("Error publishing event: {}", eventType, throwable);
					return null;
				});
	}

	@Override
	public CompletableFuture<Void> publishAll(List<DomainEvent> events) {
		Objects.requireNonNull(events, "Events cannot be null");

		List<CompletableFuture<Void>> futures = events.stream()
				.map(this::publish)
				.toList();

		return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
	}

	/**
	 * Shutdown event bus dan thread pool.
	 * Harus dipanggil saat application shutdown.
	 */
	public void shutdown() {
		if (ownedExecutor) {
			executorService.shutdown();
			try {
				if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
					executorService.shutdownNow();
					LOG.warn("Event bus executor did not terminate within timeout");
				}
			} catch (InterruptedException e) {
				executorService.shutdownNow();
				Thread.currentThread().interrupt();
				LOG.warn("Event bus executor interrupted", e);
			}
		}
	}

	/**
	 * Get number of subscribers untuk event type tertentu.
	 * Useful untuk testing dan monitoring.
	 */
	public int getSubscriberCount(String eventType) {
		return subscribers.getOrDefault(eventType, Collections.emptyList()).size();
	}

	/**
	 * Get all registered event types.
	 */
	public Set<String> getRegisteredEventTypes() {
		return new HashSet<>(subscribers.keySet());
	}
}
