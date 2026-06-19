package dexforge.plugins.input.java.data.code.decoders;

import dexforge.plugins.input.java.data.code.CodeDecodeState;

public interface IJavaInsnDecoder {
	void decode(CodeDecodeState state);

	default void skip(CodeDecodeState state) {
	}
}
