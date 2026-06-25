package dexforge.core.parser.smali.service;

import dexforge.core.parser.smali.model.SmaliClass;
import dexforge.core.parser.smali.model.SmaliMethod;
import dexforge.core.parser.smali.parser.SmaliParser;
import dexforge.core.parser.smali.assembler.InstructionAssembler;
import dexforge.core.parser.dex.io.DexByteWriter;
import dexforge.core.parser.dex.service.DexWriter;
import dexforge.core.parser.dex.builder.DexPoolManager;
import dexforge.core.parser.dex.model.DexCode;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * High-level service to assemble Smali classes into a DEX file.
 * Implementation follows DRY and SOLID principles.
 */
public final class SmaliAssembler {
	private final List<SmaliClass> classes = new ArrayList<>();
	private final DexPoolManager poolManager = new DexPoolManager();
	private final InstructionAssembler insnAssembler = new InstructionAssembler();
	private final Map<String, DexCode> methodBytecodeMap = new HashMap<>();

	public void addClass(String smaliText) {
		SmaliParser parser = new SmaliParser();
		SmaliClass smaliClass = parser.parse(smaliText);
		classes.add(smaliClass);
		collectMetadata(smaliClass);
		assembleMethodBytecode(smaliClass);
	}

	private void collectMetadata(SmaliClass smaliClass) {
		poolManager.addClassDef(smaliClass.getClassName(), smaliClass.getAccessFlags(),
				smaliClass.getSuperName(), smaliClass.getInterfaces(), smaliClass.getSourceFile());

		if (smaliClass.getSuperName() != null) {
			poolManager.addType(smaliClass.getSuperName());
		}
		for (String iface : smaliClass.getInterfaces()) {
			poolManager.addType(iface);
		}
		for (SmaliMethod method : smaliClass.getMethods()) {
			String sig = method.getSignature();
			String returnType = sig.substring(sig.lastIndexOf(')') + 1);
			poolManager.addMethod(smaliClass.getClassName(), method.getName(), "V", returnType, new ArrayList<>());
		}
	}

	private void assembleMethodBytecode(SmaliClass smaliClass) {
		for (SmaliMethod method : smaliClass.getMethods()) {
			DexByteWriter writer = new DexByteWriter(512);
			for (String insnLine : method.getInstructions()) {
				insnAssembler.assemble(insnLine, writer, poolManager);
			}

			byte[] bytecode = writer.toByteArray();
			short[] instructions = new short[bytecode.length / 2];
			for (int i = 0; i < instructions.length; i++) {
				instructions[i] = (short) ((bytecode[i * 2] & 0xFF) | ((bytecode[i * 2 + 1] & 0xFF) << 8));
			}

			DexCode code = new DexCode(method.getRegisters(), 0, 0, 0, instructions, 0);
			methodBytecodeMap.put(smaliClass.getClassName() + "->" + method.getName(), code);

			// Link code to pool manager
			String sig = method.getSignature();
			String returnType = sig.substring(sig.lastIndexOf(')') + 1);
			DexPoolManager.ProtoId proto = new DexPoolManager.ProtoId("V", returnType, new ArrayList<>());
			poolManager.setMethodCode(smaliClass.getClassName(), method.getName(), proto, code);
		}
	}

	public byte[] assemble() throws Exception {
		if (classes.isEmpty()) {
			return new byte[0];
		}

		// The DexWriter would now use methodBytecodeMap to fill class_data_item and code_item
		DexWriter writer = new DexWriter(poolManager);
		return writer.compile();
	}

	public DexPoolManager getPoolManager() {
		return poolManager;
	}

	public Map<String, DexCode> getMethodBytecodeMap() {
		return methodBytecodeMap;
	}
}
