package dexforge.domain.model.project;

import dexforge.domain.model.EntityId;

/**
 * Value Object: Project ID
 * Type-safe identifier untuk Project aggregate root.
 *
 * Immutable dan digunakan untuk refer ke project tanpa depend pada project object itu sendiri.
 */
public final class ProjectId extends EntityId<String> {
	private ProjectId(String value) {
		super(value);
	}

	/**
	 * Factory method untuk create ProjectId.
	 * Validates bahwa id tidak null dan tidak blank.
	 */
	public static ProjectId of(String id) {
		if (id == null || id.isBlank()) {
			throw new IllegalArgumentException("ProjectId cannot be empty");
		}
		return new ProjectId(id);
	}
}
