package dexforge.api.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import dexforge.api.event.DexForgeEvent;
import dexforge.api.event.DexForgeEventBus;

/**
 * Basic thread-safe implementation of DexForgeEventBus.
 */
final class DexForgeEventBusImpl implements DexForgeEventBus {
	private final Map<Class<?>, List<Consumer<?>>> subscribers = new ConcurrentHashMap<>();

	@Override
	public <T extends DexForgeEvent> void subscribe(Class<T> eventType, Consumer<T> listener) {
		subscribers.computeIfAbsent(eventType, k -> new ArrayList<>()).add(listener);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void publish(DexForgeEvent event) {
		Class<?> type = event.getClass();
		// Notify direct subscribers
		notify(type, event);

		// Notify interface subscribers (simplification)
		for (Class<?> iface : type.getInterfaces()) {
			if (DexForgeEvent.class.isAssignableFrom(iface)) {
				notify(iface, event);
			}
		}
	}

	private void notify(Class<?> type, DexForgeEvent event) {
		List<Consumer<?>> list = subscribers.get(type);
		if (list != null) {
			for (Consumer<?> consumer : list) {
				((Consumer<Object>) consumer).accept(event);
			}
		}
	}
}
