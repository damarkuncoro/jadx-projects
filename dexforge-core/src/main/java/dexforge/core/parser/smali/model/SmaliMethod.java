package dexforge.core.parser.smali.model;

import java.util.ArrayList;
import java.util.List;

public final class SmaliMethod {
	private final String name;
	private final String signature;
	private final int accessFlags;
	private final List<String> instructions = new ArrayList<>();
	private int registers = 0;

	public SmaliMethod(String name, String signature, int accessFlags) {
		this.name = name;
		this.signature = signature;
		this.accessFlags = accessFlags;
	}

	public String getName() {
		return name;
	}

	public String getSignature() {
		return signature;
	}

	public int getAccessFlags() {
		return accessFlags;
	}

	public List<String> getInstructions() {
		return instructions;
	}

	public int getRegisters() {
		return registers;
	}

	public void setRegisters(int registers) {
		this.registers = registers;
	}

	public void addInstruction(String insn) {
		this.instructions.add(insn);
	}
}
