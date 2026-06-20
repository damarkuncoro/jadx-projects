package dexforge.domain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import dexforge.domain.event.DomainEvent;

/**
 * Base class untuk Aggregate Roots dalam DDD.
 * Aggregate Root adalah entry point untuk aggregate dan responsible untuk consistency.
 * Setiap aggregate root memiliki boundary dan tidak boleh ada reference langsung ke entities dalam
 * aggregate.
 *
 * AggregateRoot mendukung Domain Event Sourcing pattern.
 */
public abstract class AggregateRoot extends Entity {
	/**
	 * Uncommitted domain events yang belum di-persist.
	 * Events akan di-publish dan di-clear setelah di-persist.
	 */
	private final List<DomainEvent> uncommittedEvents = new ArrayList<>();

	protected AggregateRoot(EntityId<?> id) {
		super(id);
	}

	/**
	 * Raise domain event. Event akan di-collect dan di-publish oleh infrastructure layer.
	 */
	protected void raise(DomainEvent event) {
		Objects.requireNonNull(event, "DomainEvent cannot be null");
		uncommittedEvents.add(event);
	}

	/**
	 * Get semua uncommitted events.
	 */
	public List<DomainEvent> getUncommittedEvents() {
		return Collections.unmodifiableList(uncommittedEvents);
	}

	/**
	 * Clear uncommitted events setelah di-persist.
	 * Dipanggil oleh infrastructure layer (repository) setelah events di-publish.
	 */
	public void markEventsAsCommitted() {
		uncommittedEvents.clear();
	}

	/**
	 * Check apakah ada uncommitted events.
	 */
	public boolean hasUncommittedEvents() {
		return !uncommittedEvents.isEmpty();
	}
}
