package dexforge.api.model;

import java.util.List;
import java.util.Optional;

import dexforge.api.model.insn.DexForgeInstruction;
import dexforge.api.model.insn.DexForgeOpcode;
import dexforge.core.infrastructure.jadx.JadxNodeHelper;

/**
 * Implementation of DexForgeInstruction that wraps JADX InsnNode.
 */
final class DexForgeInstructionImpl implements DexForgeInstruction {
	private final Object delegate;

	DexForgeInstructionImpl(Object delegate) {
		this.delegate = delegate;
	}

	@Override
	public DexForgeOpcode getOpcode() {
		String type = JadxNodeHelper.getInsnType(delegate);
		try {
			return DexForgeOpcode.valueOf(type);
		} catch (Exception e) {
			return DexForgeOpcode.UNKNOWN;
		}
	}

	@Override
	public int getOffset() {
		return JadxNodeHelper.getInsnOffset(delegate);
	}

	@Override
	public String getMnemonic() {
		return JadxNodeHelper.getInsnMnemonic(delegate);
	}

	@Override
	public Optional<DexForgeNode> getReferencedNode() {
		return Optional.ofNullable(DexForgeNodeFactory.wrap(JadxNodeHelper.getInsnReferencedNode(delegate)));
	}

	@Override
	public List<String> getOperands() {
		return JadxNodeHelper.getInsnOperands(delegate);
	}

	@Override
	public String toString() {
		return getMnemonic();
	}
}
