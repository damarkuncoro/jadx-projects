package dexforge.api.model.metadata;

/**
 * Enhanced metadata annotation for decompiled code elements.
 */
public interface DexForgeAnnotation {

	enum AnnType {
		CLASS,
		FIELD,
		METHOD,
		PACKAGE,
		VARIABLE,
		VARIABLE_REFERENCE,
		DECLARATION,
		OFFSET,
		END,
		COMMENT,
		VULNERABILITY_MARKER // Enhanced: specific type for analysis findings
	}

	AnnType getAnnType();

	/**
	 * Optional description or raw data associated with this annotation.
	 */
	default String getData() {
		return null;
	}
}
