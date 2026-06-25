package dexforge.api.model;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import dexforge.api.engine.DexForgeEngine;
import dexforge.api.model.insn.DexForgeInstruction;
import dexforge.api.model.insn.DexForgeOpcode;

/**
 * Implementation of DexForgeInstruction.
 */
final class DexForgeInstructionImpl implements DexForgeInstruction {
	private final Object delegate;
	private final DexForgeEngine engine;

	DexForgeInstructionImpl(Object delegate, DexForgeEngine engine) {
		this.delegate = delegate;
		this.engine = engine;
	}

	@Override
	public DexForgeOpcode getOpcode() {
		return DexForgeOpcode.UNKNOWN;
	}

	@Override
	public int getOffset() {
		return 0;
	}

	@Override
	public String getMnemonic() {
		return engine.getName(delegate);
	}

	@Override
	public Optional<DexForgeNode> getReferencedNode() {
		return Optional.empty();
	}

	@Override
	public List<String> getOperands() {
		return Collections.emptyList();
	}
}
