package dexforge.core.parser.smali.parser;

import dexforge.core.parser.smali.model.SmaliClass;
import dexforge.core.parser.smali.model.SmaliMethod;

/**
 * Responsible for creating the structured Smali models (AST).
 */
public final class SmaliASTBuilder {
	private final SmaliClass smaliClass = new SmaliClass();
	private SmaliMethod currentMethod;

	public void setClassName(String className) {
		smaliClass.setClassName(className);
	}

	public void setSuperName(String superName) {
		smaliClass.setSuperName(superName);
	}

	public void setSourceFile(String sourceFile) {
		smaliClass.setSourceFile(sourceFile);
	}

	public void addInterface(String interfaceName) {
		smaliClass.getInterfaces().add(interfaceName);
	}

	public void startMethod(String name, String signature, int accessFlags) {
		currentMethod = new SmaliMethod(name, signature, accessFlags);
		smaliClass.getMethods().add(currentMethod);
	}

	public void endMethod() {
		currentMethod = null;
	}

	public void setMethodRegisters(int registers) {
		if (currentMethod != null) {
			currentMethod.setRegisters(registers);
		}
	}

	public void addInstruction(String instruction) {
		if (currentMethod != null) {
			currentMethod.addInstruction(instruction);
		}
	}

	public SmaliClass getResult() {
		return smaliClass;
	}
}
