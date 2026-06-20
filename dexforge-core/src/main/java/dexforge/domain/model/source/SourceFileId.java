package dexforge.domain.model.source;

import dexforge.domain.model.EntityId;

/**
 * Value Object: SourceFile ID
 */
public final class SourceFileId extends EntityId<String> {
	private SourceFileId(String value) {
		super(value);
	}

	public static SourceFileId of(String id) {
		if (id == null || id.isBlank()) {
			throw new IllegalArgumentException("SourceFileId cannot be empty");
		}
		return new SourceFileId(id);
	}

	public static SourceFileId fromClass(String className) {
		return of(className);
	}
}
