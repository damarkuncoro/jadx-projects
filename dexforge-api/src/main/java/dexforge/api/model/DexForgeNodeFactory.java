package dexforge.api.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dexforge.api.exception.DexForgeException;
import dexforge.core.infrastructure.jadx.JadxNodeHelper;

/**
 * Internal factory for wrapping JADX nodes into DexForge nodes.
 * Although public to allow cross-package access within the module,
 * it is not considered part of the stable public API.
 */
public final class DexForgeNodeFactory {
	private DexForgeNodeFactory() {
	}

	public static DexForgeNode wrap(Object node) {
		if (node == null) {
			return null;
		}
		if (JadxNodeHelper.isClassNode(node)) {
			return new DexForgeClass(node);
		}
		if (JadxNodeHelper.isMethodNode(node)) {
			return new DexForgeMethod(node);
		}
		if (JadxNodeHelper.isFieldNode(node)) {
			return new DexForgeField(node);
		}
		// Fallback for JADX types if they leak through
		Object javaNode = JadxNodeHelper.getJavaNode(node);
		if (javaNode != null) {
			return wrap(javaNode);
		}
		// Assuming any other object that could be a package is handled by wrapPackages
		if (node.getClass().getName().contains("JavaPackage")) {
			return new DexForgePackage(node);
		}
		throw new DexForgeException("UNSUPPORTED_NODE", "Unsupported node type: " + node.getClass().getName());
	}

	public static List<DexForgeNode> wrapNodes(List<?> nodes) {
		if (nodes.isEmpty()) {
			return Collections.emptyList();
		}
		List<DexForgeNode> result = new ArrayList<>(nodes.size());
		for (Object node : nodes) {
			result.add(wrap(node));
		}
		return Collections.unmodifiableList(result);
	}

	public static List<DexForgeClass> wrapClasses(List<?> classes) {
		if (classes.isEmpty()) {
			return Collections.emptyList();
		}
		List<DexForgeClass> result = new ArrayList<>(classes.size());
		for (Object cls : classes) {
			if (JadxNodeHelper.isClassNode(cls)) {
				result.add(new DexForgeClass(cls));
			} else {
				Object internal = JadxNodeHelper.getJavaNode(cls);
				if (JadxNodeHelper.isClassNode(internal)) {
					result.add(new DexForgeClass(internal));
				}
			}
		}
		return Collections.unmodifiableList(result);
	}

	public static List<DexForgeMethod> wrapMethods(List<?> methods) {
		if (methods.isEmpty()) {
			return Collections.emptyList();
		}
		List<DexForgeMethod> result = new ArrayList<>(methods.size());
		for (Object method : methods) {
			if (JadxNodeHelper.isMethodNode(method)) {
				result.add(new DexForgeMethod(method));
			} else {
				Object internal = JadxNodeHelper.getJavaNode(method);
				if (JadxNodeHelper.isMethodNode(internal)) {
					result.add(new DexForgeMethod(internal));
				}
			}
		}
		return Collections.unmodifiableList(result);
	}

	public static List<DexForgeField> wrapFields(List<?> fields) {
		if (fields.isEmpty()) {
			return Collections.emptyList();
		}
		List<DexForgeField> result = new ArrayList<>(fields.size());
		for (Object field : fields) {
			if (JadxNodeHelper.isFieldNode(field)) {
				result.add(new DexForgeField(field));
			} else {
				Object internal = JadxNodeHelper.getJavaNode(field);
				if (JadxNodeHelper.isFieldNode(internal)) {
					result.add(new DexForgeField(internal));
				}
			}
		}
		return Collections.unmodifiableList(result);
	}

	public static DexForgeClass wrapClass(Object cls) {
		if (cls == null) {
			return null;
		}
		if (JadxNodeHelper.isClassNode(cls)) {
			return new DexForgeClass(cls);
		}
		Object internal = JadxNodeHelper.getJavaNode(cls);
		if (JadxNodeHelper.isClassNode(internal)) {
			return new DexForgeClass(internal);
		}
		return null;
	}

	public static DexForgeMethod wrapMethod(Object method) {
		if (method == null) {
			return null;
		}
		if (JadxNodeHelper.isMethodNode(method)) {
			return new DexForgeMethod(method);
		}
		Object internal = JadxNodeHelper.getJavaNode(method);
		if (JadxNodeHelper.isMethodNode(internal)) {
			return new DexForgeMethod(internal);
		}
		return null;
	}

	public static List<DexForgePackage> wrapPackages(List<?> packages) {
		if (packages.isEmpty()) {
			return Collections.emptyList();
		}
		List<DexForgePackage> result = new ArrayList<>(packages.size());
		for (Object pkg : packages) {
			result.add(new DexForgePackage(pkg));
		}
		return Collections.unmodifiableList(result);
	}

	static DexForgeCodeMetadata createMetadata(Object codeInfo) {
		return new DexForgeMetadataImpl(codeInfo);
	}
}
