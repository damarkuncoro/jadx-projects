package dexforge.api.engine;

import dexforge.api.model.DexForgeApkMetadata;
import dexforge.api.intelligence.IProjectIntelligence;
import dexforge.api.ui.IUiEditor;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Interface that any decompiler engine (JADX, FernFlower, etc.) must implement.
 * This ensures Core is 100% independent of specific implementations.
 */
public interface DexForgeEngine {
	String getEngineId();

	/**
	 * Get intelligence service for this engine.
	 */
	default IProjectIntelligence getIntelligence() {
		return null;
	}

	/**
	 * Get UI editor for this engine.
	 */
	default IUiEditor getUiEditor(Object rootNode) {
		return null;
	}

	/**
	 * Initialize the engine.
	 */
	void init(List<File> inputFiles, Map<String, Object> settings);

	/**
	 * Set a custom code cache for the engine.
	 */
	default void setCodeCache(Object codeCache) {
		// Optional: only if engine supports external caching
	}

	/**
	 * Trigger decompilation process.
	 */
	void load();

	/**
	 * Node Discovery.
	 */
	List<Object> getRawClasses();
	List<Object> getRawPackages();
	List<Object> getClassesInPackage(Object rawPackage);
	List<Object> getSubPackages(Object rawPackage);
	List<Object> getRawResources();

	/**
	 * Node Interaction (Agnostic).
	 */
	String getName(Object rawNode);
	String getFullName(Object rawNode);
	String getCode(Object rawNode);
	String getSmali(Object rawNode);
	String getResourceText(Object rawResource);
	String decodeBinaryXml(byte[] bytes);

	/**
	 * Type-specific data.
	 */
	List<Object> getMethods(Object rawClass);
	List<Object> getFields(Object rawClass);
	List<Object> getInnerClasses(Object rawClass);
	String getSuperClass(Object rawClass);
	List<String> getInterfaces(Object rawClass);

	String getReturnType(Object rawMethod);
	List<String> getArgumentTypes(Object rawMethod);
	boolean isConstructor(Object rawMethod);

	String getFieldType(Object rawField);

	/**
	 * Access modifiers (bitmask using java.lang.reflect.Modifier compatible values).
	 */
	int getModifiers(Object rawNode);

	/**
	 * Rename a node.
	 */
	void rename(Object rawNode, String newName);

	/**
	 * Remove any alias/rename from a node.
	 */
	void removeAlias(Object rawNode);

	/**
	 * Cross-reference navigation.
	 */
	List<Object> getUseIn(Object rawNode);

	/**
	 * Find a node at a specific position in the decompiled code of a class.
	 */
	Object getNodeAt(Object rawClass, int position);

	/**
	 * Get metadata annotation at a specific position.
	 */
	Object getAnnotationAt(Object rawCodeInfo, int position);

	/**
	 * Get all annotations from raw code info.
	 */
	Map<Integer, Object> getAnnotations(Object rawCodeInfo);

	/**
	 * Get the type name of an annotation.
	 */
	String getAnnotationType(Object rawAnnotation);

	/**
	 * Get additional data from an annotation.
	 */
	String getAnnotationData(Object rawAnnotation);

	/**
	 * Get the definition position (offset) of a node in its source code.
	 */
	int getDefinitionPosition(Object rawNode);

	/**
	 * Search for a class by its name.
	 */
	Object searchClass(String fullName);

	/**
	 * Get metadata for the loaded APK (if applicable).
	 */
	dexforge.api.model.DexForgeApkMetadata getApkMetadata();

	/**
	 * Calculate a fingerprint (SHA-256) of input files for integrity verification.
	 */
	Map<String, String> calculateFingerprint();

	void close();
}
