package dexforge.core.parser.smali.service;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import dexforge.core.parser.dex.model.DexClass;
import dexforge.core.parser.dex.model.DexEncodedMethod;
import dexforge.core.parser.dex.model.DexCode;
import dexforge.core.parser.dex.model.DexInstruction;
import dexforge.core.parser.dex.model.DexAccessFlags;
import dexforge.core.parser.dex.service.DexFastIndexer;
import dexforge.core.parser.dex.sections.DexCodeParser;
import dexforge.core.parser.dex.sections.DexInstructionDecoder;
import dexforge.core.parser.dex.sections.DexOpcode;

/**
 * Generator for Smali representation of DEX components.
 * Supports instruction patching for deobfuscation.
 */
public final class SmaliWriter {
	private final DexFastIndexer indexer;
	private final Map<String, String> patches = new HashMap<>();

	public SmaliWriter(DexFastIndexer indexer) {
		this.indexer = indexer;
	}

	/**
	 * Registers a patch for a specific instruction at a specific location.
	 * location format: ClassName->MethodName(Proto)@Offset
	 */
	public void addPatch(String location, String smaliInstruction) {
		patches.put(location, smaliInstruction);
	}

	public String writeClass(DexClass clazz) {
		StringBuilder sb = new StringBuilder();
		sb.append(".class ").append(clazz.getAccessFlagsString()).append(" ").append(clazz.getName()).append("\n");
		sb.append(".super ").append(clazz.getSuperclass()).append("\n");

		if (clazz.getSourceFile() != null) {
			sb.append(".source \"").append(clazz.getSourceFile()).append("\"\n");
		}
		sb.append("\n");

		for (String iface : clazz.getInterfaces()) {
			sb.append(".implements ").append(iface).append("\n");
		}
		sb.append("\n");

		indexer.fillClassData(clazz);
		var data = clazz.getClassData();
		if (data != null) {
			for (var method : data.directMethods) {
				sb.append(writeMethod(clazz, method));
			}
			for (var method : data.virtualMethods) {
				sb.append(writeMethod(clazz, method));
			}
		}

		return sb.toString();
	}

	public String writeMethod(DexClass clazz, DexEncodedMethod method) {
		StringBuilder sb = new StringBuilder();
		String sig = indexer.getMethodPool().getMethodSignature(method.getMethodIndex());
		String name = indexer.getMethodPool().getMethodName(method.getMethodIndex());

		sb.append(".method ").append(DexAccessFlags.format(method.getAccessFlags())).append(" ").append(name);
		String proto = sig.substring(sig.indexOf('('));
		sb.append(proto).append("\n");

		if (method.getCodeOff() != 0) {
			DexCode code = indexer.getCodeParser().parse(method.getCodeOff());
			sb.append("    .registers ").append(code.getRegistersSize()).append("\n\n");

			Map<Integer, Integer> lines = indexer.getLineNumbers(method);
			List<DexInstruction> insns = DexInstructionDecoder.decode(code);

			for (DexInstruction insn : insns) {
				if (lines.containsKey(insn.getOffset())) {
					sb.append("    .line ").append(lines.get(insn.getOffset())).append("\n");
				}

				String location = clazz.getName() + "->" + sig + "@" + insn.getOffset();
				if (patches.containsKey(location)) {
					sb.append("    ").append(patches.get(location)).append(" # patched\n");
				} else {
					sb.append("    ").append(formatInstruction(insn)).append("\n");
				}
			}
		}

		sb.append(".end method\n\n");
		return sb.toString();
	}

	private String formatInstruction(DexInstruction insn) {
		String mnemonic = DexOpcode.getMnemonic(insn.getOpcode());
		int[] regs = insn.getRegisters();
		int index = insn.getIndex();

		StringBuilder sb = new StringBuilder(mnemonic);
		sb.append(" ");

		// Format registers
		if (regs != null && regs.length > 0) {
			for (int i = 0; i < regs.length; i++) {
				sb.append("v").append(regs[i]);
				if (i < regs.length - 1) sb.append(", ");
			}
			if (index != -1 || (insn.getOpcode() & 0xFF) >= 0x12 && (insn.getOpcode() & 0xFF) <= 0x15) {
				sb.append(", ");
			}
		}

		// Format index/literal
		int op = insn.getOpcode() & 0xFF;
		if (op == 0x1A || op == 0x1B) { // const-string
			if (index >= 0 && index < indexer.getStringPool().getSize()) {
				String str = indexer.getStringPool().getString(index);
				sb.append("\"").append(escapeString(str)).append("\"");
			} else {
				sb.append("idx_").append(index).append(" # invalid");
			}
		} else if (op >= 0x6E && op <= 0x72) { // invoke
			if (index >= 0 && index < indexer.getMethodPool().getSize()) {
				String methSig = indexer.getMethodPool().getMethodSignature(index);
				sb.append(methSig);
			} else {
				sb.append("meth_idx_").append(index);
			}
		} else if (op >= 0x12 && op <= 0x15) { // const
			sb.append("0x").append(Long.toHexString(insn.getLiteral()));
		} else if (index != -1) {
			sb.append("idx_").append(index);
		}

		return sb.toString().trim();
	}

	private String escapeString(String s) {
		if (s == null) return "null";
		return s.replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t").replace("\"", "\\\"");
	}
}
