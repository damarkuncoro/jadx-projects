package dexforge.infrastructure.event;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dexforge.domain.event.DomainEvent;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests untuk SimpleEventBus.
 * Menunjukkan how event bus delivers events secara asynchronous.
 */
public class SimpleEventBusTest {
	private SimpleEventBus eventBus;

	@BeforeEach
	public void setUp() {
		eventBus = new SimpleEventBus();
	}

	@Test
	public void testSubscribeAndPublish() throws Exception {
		// Given
		AtomicInteger counter = new AtomicInteger(0);
		DomainEventListener listener = new DomainEventListener() {
			@Override
			public CompletableFuture<Void> handle(DomainEvent event) {
				counter.incrementAndGet();
				return CompletableFuture.completedFuture(null);
			}

			@Override
			public boolean supports(String eventType) {
				return "TestEvent".equals(eventType);
			}
		};

		// When
		eventBus.subscribe("TestEvent", listener);
		DomainEvent event = new TestEvent();
		eventBus.publish(event).join();

		// Then: Listener should be called
		Thread.sleep(100); // Wait for async execution
		assertThat(counter.get()).isEqualTo(1);
	}

	@Test
	public void testMultipleListeners() throws Exception {
		// Given
		AtomicInteger listener1Counter = new AtomicInteger(0);
		AtomicInteger listener2Counter = new AtomicInteger(0);

		DomainEventListener listener1 = new DomainEventListener() {
			@Override
			public CompletableFuture<Void> handle(DomainEvent event) {
				listener1Counter.incrementAndGet();
				return CompletableFuture.completedFuture(null);
			}

			@Override
			public boolean supports(String eventType) {
				return "TestEvent".equals(eventType);
			}
		};

		DomainEventListener listener2 = new DomainEventListener() {
			@Override
			public CompletableFuture<Void> handle(DomainEvent event) {
				listener2Counter.incrementAndGet();
				return CompletableFuture.completedFuture(null);
			}

			@Override
			public boolean supports(String eventType) {
				return "TestEvent".equals(eventType);
			}
		};

		// When
		eventBus.subscribe("TestEvent", listener1);
		eventBus.subscribe("TestEvent", listener2);
		DomainEvent event = new TestEvent();
		eventBus.publish(event).join();

		// Then
		Thread.sleep(100);
		assertThat(listener1Counter.get()).isEqualTo(1);
		assertThat(listener2Counter.get()).isEqualTo(1);
	}

	@Test
	public void testUnsubscribe() throws Exception {
		// Given
		AtomicInteger counter = new AtomicInteger(0);
		DomainEventListener listener = new DomainEventListener() {
			@Override
			public CompletableFuture<Void> handle(DomainEvent event) {
				counter.incrementAndGet();
				return CompletableFuture.completedFuture(null);
			}

			@Override
			public boolean supports(String eventType) {
				return "TestEvent".equals(eventType);
			}
		};

		eventBus.subscribe("TestEvent", listener);

		// When: Unsubscribe and publish
		eventBus.unsubscribe("TestEvent", listener);
		DomainEvent event = new TestEvent();
		eventBus.publish(event).join();

		// Then: Listener should NOT be called
		Thread.sleep(100);
		assertThat(counter.get()).isEqualTo(0);
	}

	@Test
	public void testGetSubscriberCount() {
		// Given
		DomainEventListener listener1 = createTestListener();
		DomainEventListener listener2 = createTestListener();

		// When
		eventBus.subscribe("TestEvent", listener1);
		eventBus.subscribe("TestEvent", listener2);

		// Then
		assertThat(eventBus.getSubscriberCount("TestEvent")).isEqualTo(2);
	}

	@Test
	public void testShutdown() {
		// When
		eventBus.shutdown();

		// Then: No exception should be thrown
		assertThatNoException().isThrownBy(() -> eventBus.shutdown());
	}

	// Helper methods

	private DomainEventListener createTestListener() {
		return new DomainEventListener() {
			@Override
			public CompletableFuture<Void> handle(DomainEvent event) {
				return CompletableFuture.completedFuture(null);
			}

			@Override
			public boolean supports(String eventType) {
				return true;
			}
		};
	}

	// Test event implementation
	private static class TestEvent implements DomainEvent {
		private final LocalDateTime occurredAt = LocalDateTime.now();

		@Override
		public String getEventType() {
			return "TestEvent";
		}

		@Override
		public LocalDateTime getOccurredAt() {
			return occurredAt;
		}
	}
}
