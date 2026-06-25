package dexforge.api.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dexforge.api.engine.DexForgeEngine;
import dexforge.api.exception.DexForgeException;

/**
 * Internal factory for wrapping raw engine nodes into DexForge nodes.
 */
public final class DexForgeNodeFactory {
	private DexForgeNodeFactory() {
	}

	public static DexForgeNode wrap(Object rawNode, DexForgeEngine engine) {
		if (rawNode == null) return null;
		// Determine type from raw node if possible, or fallback to class
		return new DexForgeClass(rawNode, engine);
	}

	public static DexForgeClass wrapClass(Object rawClass, DexForgeEngine engine) {
		return rawClass == null ? null : new DexForgeClass(rawClass, engine);
	}

	public static List<DexForgeClass> wrapClasses(List<?> rawClasses, DexForgeEngine engine) {
		if (rawClasses.isEmpty()) {
			return Collections.emptyList();
		}
		List<DexForgeClass> result = new ArrayList<>(rawClasses.size());
		for (Object raw : rawClasses) {
			result.add(new DexForgeClass(raw, engine));
		}
		return Collections.unmodifiableList(result);
	}

	public static List<DexForgePackage> wrapPackages(List<?> rawPackages, DexForgeEngine engine) {
		if (rawPackages.isEmpty()) {
			return Collections.emptyList();
		}
		List<DexForgePackage> result = new ArrayList<>(rawPackages.size());
		for (Object raw : rawPackages) {
			result.add(new DexForgePackage(raw, engine));
		}
		return Collections.unmodifiableList(result);
	}

	public static DexForgeMethod wrapMethod(Object rawMethod, DexForgeEngine engine) {
		return rawMethod == null ? null : new DexForgeMethod(rawMethod, engine);
	}

	public static List<DexForgeMethod> wrapMethods(List<?> rawMethods, DexForgeEngine engine) {
		if (rawMethods.isEmpty()) {
			return Collections.emptyList();
		}
		List<DexForgeMethod> result = new ArrayList<>(rawMethods.size());
		for (Object raw : rawMethods) {
			result.add(new DexForgeMethod(raw, engine));
		}
		return Collections.unmodifiableList(result);
	}

	public static List<DexForgeField> wrapFields(List<?> rawFields, DexForgeEngine engine) {
		if (rawFields.isEmpty()) {
			return Collections.emptyList();
		}
		List<DexForgeField> result = new ArrayList<>(rawFields.size());
		for (Object raw : rawFields) {
			result.add(new DexForgeField(raw, engine));
		}
		return Collections.unmodifiableList(result);
	}

	public static List<DexForgeNode> wrapNodes(List<?> rawNodes, DexForgeEngine engine) {
		if (rawNodes.isEmpty()) {
			return Collections.emptyList();
		}
		List<DexForgeNode> result = new ArrayList<>(rawNodes.size());
		for (Object raw : rawNodes) {
			// This is a bit tricky as we don't know the type.
			// In a real implementation, the engine could provide type info.
			result.add(new DexForgeClass(raw, engine)); // Fallback
		}
		return Collections.unmodifiableList(result);
	}

	static DexForgeCodeMetadata createMetadata(Object codeInfo, DexForgeEngine engine) {
		return new DexForgeMetadataImpl(codeInfo, engine);
	}
}
