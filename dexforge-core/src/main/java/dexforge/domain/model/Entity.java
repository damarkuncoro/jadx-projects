package dexforge.domain.model;

import java.util.Objects;

/**
 * Base class untuk Domain Entities dalam DDD.
 * Entity memiliki identity yang unik (ID).
 * Dua entities dianggap sama jika memiliki ID yang sama, meskipun atribut lainnya berbeda.
 */
public abstract class Entity {
	/**
	 * Identity unik untuk entity ini.
	 * Type-safe ID menggunakan value object (subclass dari EntityId).
	 */
	protected final EntityId<?> id;

	protected Entity(EntityId<?> id) {
		this.id = Objects.requireNonNull(id, "Entity ID cannot be null");
	}

	public EntityId<?> getId() {
		return id;
	}

	/**
	 * Entities diidentifikasi berdasarkan ID, bukan atribut lainnya.
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Entity entity = (Entity) o;
		return Objects.equals(id, entity.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "{"
				+ "id=" + id
				+ '}';
	}
}
