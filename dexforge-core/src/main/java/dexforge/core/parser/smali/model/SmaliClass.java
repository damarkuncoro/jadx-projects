package dexforge.core.parser.smali.model;

import java.util.ArrayList;
import java.util.List;

public final class SmaliClass {
	private String className;
	private String superName;
	private String sourceFile;
	private int accessFlags;
	private final List<String> interfaces = new ArrayList<>();
	private final List<SmaliMethod> methods = new ArrayList<>();

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getSuperName() {
		return superName;
	}

	public void setSuperName(String superName) {
		this.superName = superName;
	}

	public String getSourceFile() {
		return sourceFile;
	}

	public void setSourceFile(String sourceFile) {
		this.sourceFile = sourceFile;
	}

	public int getAccessFlags() {
		return accessFlags;
	}

	public void setAccessFlags(int accessFlags) {
		this.accessFlags = accessFlags;
	}

	public List<String> getInterfaces() {
		return interfaces;
	}

	public List<SmaliMethod> getMethods() {
		return methods;
	}
}
