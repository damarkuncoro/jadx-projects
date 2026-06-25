package dexforge.core.parser.dex.model;

/**
 * Basic representation of a decoded instruction.
 */
public final class DexInstruction {
	private final int opcode;
	private final int index; // Reference index (string_idx, method_idx, etc.)
	private final int offset; // Offset within the instructions array (in 16-bit units)
	private final int length; // Length of the instruction (in 16-bit units)
	private final short[] units;
	private int[] registers;
	private long literal;

	public DexInstruction(int opcode, int index, int offset) {
		this(opcode, index, offset, 1, null);
	}

	public DexInstruction(int opcode, int index, int offset, int length) {
		this(opcode, index, offset, length, null);
	}

	public DexInstruction(int opcode, int index, int offset, int length, short[] units) {
		this.opcode = opcode;
		this.index = index;
		this.offset = offset;
		this.length = length;
		this.units = units;
	}

	public int getOpcode() {
		return opcode;
	}

	public int getIndex() {
		return index;
	}

	public int getOffset() {
		return offset;
	}

	public int getLength() {
		return length;
	}

	public short[] getUnits() {
		return units;
	}

	public int[] getRegisters() {
		return registers;
	}

	public void setRegisters(int[] registers) {
		this.registers = registers;
	}

	public long getLiteral() {
		return literal;
	}

	public void setLiteral(long literal) {
		this.literal = literal;
	}

	@Override
	public String toString() {
		return String.format("0x%02X at %d (idx: %d, len: %d)", opcode, offset, index, length);
	}
}
