package dexforge.domain.model;

import java.io.Serializable;

/**
 * Base class untuk type-safe IDs dalam DDD.
 * Setiap aggregate root harus memiliki ID yang extends class ini.
 *
 * Example:
 *
 * <pre>
 * public final class ProjectId extends EntityId&lt;String&gt; {
 * 	public ProjectId(String value) {
 * 		super(value);
 * 	}
 * }
 * </pre>
 */
public abstract class EntityId<T> implements Serializable {
	protected final T value;

	protected EntityId(T value) {
		if (value == null) {
			throw new IllegalArgumentException(this.getClass().getSimpleName() + " value cannot be null");
		}
		this.value = value;
	}

	public T getValue() {
		return value;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		EntityId<?> entityId = (EntityId<?>) o;
		return value.equals(entityId.value);
	}

	@Override
	public int hashCode() {
		return value.hashCode();
	}

	@Override
	public String toString() {
		return value.toString();
	}
}
