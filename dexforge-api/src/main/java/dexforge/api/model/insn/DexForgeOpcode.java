package dexforge.api.model.insn;

/**
 * Standardized Opcodes for DexForge analysis.
 * Simplifies complex engine-specific opcodes into high-level categories.
 */
public enum DexForgeOpcode {
	NOP,
	MOVE,
	CONST,
	LOAD,
	STORE,
	ARITHMETIC,
	LOGIC,
	CAST,
	IF,
	GOTO,
	SWITCH,
	INVOKE,
	RETURN,
	GET_FIELD,
	PUT_FIELD,
	NEW_INSTANCE,
	NEW_ARRAY,
	THROW,
	MONITOR_ENTER,
	MONITOR_EXIT,
	UNKNOWN
}
