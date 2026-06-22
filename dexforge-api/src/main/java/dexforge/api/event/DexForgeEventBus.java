package dexforge.api.event;

import java.util.function.Consumer;

/**
 * A simple event bus for DexForge.
 * Allows decoupling components by subscribing to specific event types.
 */
public interface DexForgeEventBus {
	/**
	 * Subscribe to events of a specific type.
	 */
	<T extends DexForgeEvent> void subscribe(Class<T> eventType, Consumer<T> listener);

	/**
	 * Publish an event to all interested subscribers.
	 */
	void publish(DexForgeEvent event);
}
