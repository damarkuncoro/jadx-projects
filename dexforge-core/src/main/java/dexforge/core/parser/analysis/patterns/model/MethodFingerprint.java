package dexforge.core.parser.analysis.patterns.model;

/**
 * REUSEABLE: Represents the instruction profile of a method.
 * Used for heuristic detection of obfuscation patterns.
 */
public final class MethodFingerprint {
    private int bitwiseCount = 0;
    private int arithmeticCount = 0;
    private int invokeCount = 0;
    private int arrayOpCount = 0;
    private int totalInstructions = 0;

    public void addBitwise() { bitwiseCount++; totalInstructions++; }
    public void addArithmetic() { arithmeticCount++; totalInstructions++; }
    public void addInvoke() { invokeCount++; totalInstructions++; }
    public void addArrayOp() { arrayOpCount++; totalInstructions++; }
    public void addOther() { totalInstructions++; }

    public int getBitwiseCount() { return bitwiseCount; }
    public int getArithmeticCount() { return arithmeticCount; }
    public int getInvokeCount() { return invokeCount; }
    public int getArrayOpCount() { return arrayOpCount; }
    public int getTotalInstructions() { return totalInstructions; }

    public double getBitwiseDensity() {
        return totalInstructions == 0 ? 0 : (double) bitwiseCount / totalInstructions;
    }
}
