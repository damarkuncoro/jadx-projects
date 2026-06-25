package dexforge.core.parser.dex.model;

/**
 * Represents the bytecode and metadata of a method.
 */
public final class DexCode {
    private final int registersSize;
    private final int insSize;
    private final int outsSize;
    private final int debugInfoOff;
    private final short[] instructions;
    private final int triesSize;

    public DexCode(int registersSize, int insSize, int outsSize, int debugInfoOff, short[] instructions, int triesSize) {
        this.registersSize = registersSize;
        this.insSize = insSize;
        this.outsSize = outsSize;
        this.debugInfoOff = debugInfoOff;
        this.instructions = instructions;
        this.triesSize = triesSize;
    }

    public int getRegistersSize() { return registersSize; }
    public int getInsSize() { return insSize; }
    public int getOutsSize() { return outsSize; }
    public int getDebugInfoOff() { return debugInfoOff; }
    public short[] getInstructions() { return instructions; }
    public int getTriesSize() { return triesSize; }
    public boolean hasTries() { return triesSize > 0; }
}
