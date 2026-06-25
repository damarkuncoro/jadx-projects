package dexforge.core.parser.dex.model;

import java.util.List;

/**
 * Represents a class definition in the DEX file.
 */
public final class DexClass {
	private final String name;
	private final int accessFlags;
	private final String superclass;
	private final List<String> interfaces;
	private final String sourceFile;
	private final int classDataOff;
	private final int annotationsOff;
	private final int staticValuesOff;
	private dexforge.core.parser.dex.sections.DexClassDataParser.ClassData classData;

	public DexClass(String name, int accessFlags, String superclass, List<String> interfaces, String sourceFile, int classDataOff, int annotationsOff, int staticValuesOff) {
		this.name = name;
		this.accessFlags = accessFlags;
		this.superclass = superclass;
		this.interfaces = interfaces;
		this.sourceFile = sourceFile;
		this.classDataOff = classDataOff;
		this.annotationsOff = annotationsOff;
		this.staticValuesOff = staticValuesOff;
	}

	public String getName() { return name; }
	public int getAccessFlags() { return accessFlags; }
	public String getSuperclass() { return superclass; }
	public List<String> getInterfaces() { return interfaces; }
	public String getSourceFile() { return sourceFile; }
	public int getClassDataOff() { return classDataOff; }
	public int getAnnotationsOff() { return annotationsOff; }
	public int getStaticValuesOff() { return staticValuesOff; }

	public dexforge.core.parser.dex.sections.DexClassDataParser.ClassData getClassData() {
		return classData;
	}

	public void setClassData(dexforge.core.parser.dex.sections.DexClassDataParser.ClassData classData) {
		this.classData = classData;
	}

	public boolean isPublic() { return (accessFlags & 0x1) != 0; }
	public boolean isFinal() { return (accessFlags & 0x10) != 0; }
	public boolean isInterface() { return (accessFlags & 0x200) != 0; }
	public boolean isAbstract() { return (accessFlags & 0x400) != 0; }

	public String getAccessFlagsString() {
		return DexAccessFlags.format(accessFlags);
	}

	@Override
	public String toString() {
		return "DexClass{" + getAccessFlagsString() + " " + name + ", super=" + superclass + "}";
	}
}
