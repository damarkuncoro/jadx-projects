package dexforge.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jadx.api.JavaClass;
import jadx.api.JavaField;
import jadx.api.JavaMethod;
import jadx.api.JavaNode;
import jadx.api.JavaPackage;

final class DexForgeNodeFactory {
	private DexForgeNodeFactory() {
	}

	static DexForgeNode wrap(JavaNode node) {
		if (node instanceof JavaClass) {
			return new DexForgeClass((JavaClass) node);
		}
		if (node instanceof JavaMethod) {
			return new DexForgeMethod((JavaMethod) node);
		}
		if (node instanceof JavaField) {
			return new DexForgeField((JavaField) node);
		}
		if (node instanceof JavaPackage) {
			return new DexForgePackage((JavaPackage) node);
		}
		throw new DexForgeException("UNSUPPORTED_NODE", "Unsupported Java node type: " + node.getClass().getName());
	}

	static List<DexForgeNode> wrapNodes(List<JavaNode> nodes) {
		if (nodes.isEmpty()) {
			return Collections.emptyList();
		}
		List<DexForgeNode> result = new ArrayList<>(nodes.size());
		for (JavaNode node : nodes) {
			result.add(wrap(node));
		}
		return Collections.unmodifiableList(result);
	}

	static List<DexForgeClass> wrapClasses(List<JavaClass> classes) {
		if (classes.isEmpty()) {
			return Collections.emptyList();
		}
		List<DexForgeClass> result = new ArrayList<>(classes.size());
		for (JavaClass cls : classes) {
			result.add(new DexForgeClass(cls));
		}
		return Collections.unmodifiableList(result);
	}

	static List<DexForgeMethod> wrapMethods(List<JavaMethod> methods) {
		if (methods.isEmpty()) {
			return Collections.emptyList();
		}
		List<DexForgeMethod> result = new ArrayList<>(methods.size());
		for (JavaMethod method : methods) {
			result.add(new DexForgeMethod(method));
		}
		return Collections.unmodifiableList(result);
	}

	static List<DexForgeField> wrapFields(List<JavaField> fields) {
		if (fields.isEmpty()) {
			return Collections.emptyList();
		}
		List<DexForgeField> result = new ArrayList<>(fields.size());
		for (JavaField field : fields) {
			result.add(new DexForgeField(field));
		}
		return Collections.unmodifiableList(result);
	}

	static List<DexForgePackage> wrapPackages(List<JavaPackage> packages) {
		if (packages.isEmpty()) {
			return Collections.emptyList();
		}
		List<DexForgePackage> result = new ArrayList<>(packages.size());
		for (JavaPackage pkg : packages) {
			result.add(new DexForgePackage(pkg));
		}
		return Collections.unmodifiableList(result);
	}
}
