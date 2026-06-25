package dexforge.api.model;

import dexforge.api.model.metadata.DexForgeAnnotation;

/**
 * Interface for generating and formatting decompiled code with metadata support.
 */
public interface IDexForgeCodeWriter {

	IDexForgeCodeWriter startLine();

	IDexForgeCodeWriter startLine(int lineNumber);

	IDexForgeCodeWriter add(String code);

	IDexForgeCodeWriter add(char c);

	IDexForgeCodeWriter indent();

	/**
	 * Attach metadata to the current writing position.
	 */
	IDexForgeCodeWriter attachAnnotation(DexForgeAnnotation annotation);

	String getCode();

	ICodeInfo finish();

	int getLine();

	int getColumn();
}
