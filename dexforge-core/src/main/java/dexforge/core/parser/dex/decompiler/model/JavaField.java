package dexforge.core.parser.dex.decompiler.model;

import dexforge.core.parser.dex.model.DexAccessFlags;

public final class JavaField {
	private final String name;
	private final String type;
	private final int accessFlags;
	private final java.util.List<String> annotations = new java.util.ArrayList<>();

	public JavaField(String name, String type, int accessFlags) {
		this.name = name;
		this.type = type;
		this.accessFlags = accessFlags;
	}

	public java.util.List<String> getAnnotations() {
		return annotations;
	}

	public String toCode() {
		StringBuilder sb = new StringBuilder();
		for (String ann : annotations) {
			sb.append("    ").append(ann).append("\n");
		}
		sb.append("    ").append(DexAccessFlags.format(accessFlags)).append(" ").append(type).append(" ").append(name).append(";");
		return sb.toString();
	}
}
