package dexforge.domain.model;

/**
 * Base marker interface untuk Value Objects dalam DDD.
 * Value Objects adalah immutable objects yang tidak memiliki identity.
 * Dua value objects dianggap sama jika semua atributnya sama.
 *
 * Implementasi harus:
 * - Immutable (final fields, private constructor)
 * - Override equals() dan hashCode() berdasarkan semua field
 * - Override toString()
 * - Tidak memiliki setter methods
 */
public interface ValueObject {
	// Marker interface, no methods
	// Implementasi harus override equals(), hashCode(), toString()
}
