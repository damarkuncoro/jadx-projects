package dexforge.core.parser.dex.decompiler.model;

import java.util.ArrayList;
import java.util.List;
import dexforge.core.parser.dex.model.DexAccessFlags;

public final class JavaClass {
	private final String packageName;
	private final String className;
	private final String superName;
	private final int accessFlags;
	private final List<String> interfaces = new ArrayList<>();
	private final List<JavaField> fields = new ArrayList<>();
	private final List<JavaMethod> methods = new ArrayList<>();
	private final List<JavaClass> innerClasses = new ArrayList<>();
	private final List<String> annotations = new ArrayList<>();

	public JavaClass(String packageName, String className, String superName, int accessFlags) {
		this.packageName = packageName;
		this.className = className;
		this.superName = superName;
		this.accessFlags = accessFlags;
	}

	public List<JavaField> getFields() {
		return fields;
	}

	public List<JavaMethod> getMethods() {
		return methods;
	}

	public List<JavaClass> getInnerClasses() {
		return innerClasses;
	}

	public List<String> getInterfaces() {
		return interfaces;
	}

	public List<String> getAnnotations() {
		return annotations;
	}

	public String toCode() {
		StringBuilder sb = new StringBuilder();
		if (packageName != null && !packageName.isEmpty()) {
			sb.append("package ").append(packageName).append(";\n\n");
		}

		for (String ann : annotations) {
			sb.append(ann).append("\n");
		}

		sb.append(DexAccessFlags.format(accessFlags)).append(" class ").append(className);
		if (superName != null && !superName.equals("Ljava/lang/Object;")) {
			sb.append(" extends ").append(superName);
		}
		if (!interfaces.isEmpty()) {
			sb.append(" implements ").append(String.join(", ", interfaces));
		}
		sb.append(" {\n\n");

		for (JavaField field : fields) {
			sb.append(field.toCode()).append("\n");
		}
		sb.append("\n");
		for (JavaMethod method : methods) {
			sb.append(method.toCode()).append("\n");
		}

		for (JavaClass inner : innerClasses) {
			sb.append(inner.toCodeInternal(1)).append("\n");
		}

		sb.append("}\n");
		return sb.toString();
	}

	private String toCodeInternal(int indent) {
		String space = "\t".repeat(indent);
		StringBuilder sb = new StringBuilder();
		for (String ann : annotations) {
			sb.append(space).append(ann).append("\n");
		}
		sb.append(space).append(DexAccessFlags.format(accessFlags)).append(" class ").append(className).append(" {\n");

		for (JavaMethod method : methods) {
			sb.append(method.toCode());
		}

		sb.append(space).append("}\n");
		return sb.toString();
	}
}
