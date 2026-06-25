package dexforge.core.parser.analysis.emulator;

import dexforge.core.parser.dex.model.DexInstruction;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * SOLID: Manages runtime state with encapsulated logic to reduce conditional complexity in callers.
 */
public final class EmulatorState {
    private final Map<Integer, Object> registers = new HashMap<>();
    private final Map<String, Object> staticFields = new HashMap<>();
    private final Map<Object, Map<String, Object>> instanceFields = new IdentityHashMap<>();
    private Object lastResult;

    public void clear() {
        registers.clear();
        lastResult = null;
    }

    public void setRegister(int reg, Object value) {
        registers.put(reg, value);
    }

    public Object getRegister(int reg) {
        return registers.get(reg);
    }

    public void recordResult(String signature, DexInstruction insn, Object result) {
        if (signature == null) return;

        if (signature.contains("-><init>")) {
            assignConstructorResult(insn, result);
        } else {
            this.lastResult = result;
        }
    }

    private void assignConstructorResult(DexInstruction insn, Object result) {
        short[] units = insn.getUnits();
        if (units != null && units.length >= 3) {
            int receiverReg = units[2] & 0x0F;
            setRegister(receiverReg, result);
        }
    }

    public Object getInstanceField(Object instance, String sig) {
        if (instance == null || sig == null) return null;
        return instanceFields.getOrDefault(instance, new HashMap<>()).get(sig);
    }

    public void setInstanceField(Object instance, String sig, Object val) {
        if (instance == null || sig == null) return;
        instanceFields.computeIfAbsent(instance, k -> new HashMap<>()).put(sig, val);
    }

    public Object getStaticField(String sig) {
        return sig != null ? staticFields.get(sig) : null;
    }

    public void setStaticField(String sig, Object val) {
        if (sig != null) staticFields.put(sig, val);
    }

    public Map<Integer, Object> getRegisters() { return registers; }
    public Object getLastResult() { return lastResult; }
    public void setLastResult(Object val) { this.lastResult = val; }
}
