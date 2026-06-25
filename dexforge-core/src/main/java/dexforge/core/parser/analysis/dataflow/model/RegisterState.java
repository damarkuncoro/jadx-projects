package dexforge.core.parser.analysis.dataflow.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Tracks the state of registers, including potential values, types, and taint status.
 */
public final class RegisterState {
    private final Map<Integer, Object> values = new HashMap<>();
    private final Map<Integer, String> types = new HashMap<>();
    private final Map<Integer, Boolean> tainted = new HashMap<>();

    public void set(int reg, Object value, String type) {
        set(reg, value, type, false);
    }

    public void set(int reg, Object value, String type, boolean isTainted) {
        values.put(reg, value);
        types.put(reg, type);
        tainted.put(reg, isTainted);
    }

    public Object getValue(int reg) { return values.get(reg); }
    public String getType(int reg) { return types.get(reg); }
    public boolean isTainted(int reg) { return tainted.getOrDefault(reg, false); }

    public void clear(int reg) {
        values.remove(reg);
        types.remove(reg);
        tainted.remove(reg);
    }

    public RegisterState copy() {
        RegisterState copy = new RegisterState();
        copy.values.putAll(this.values);
        copy.types.putAll(this.types);
        copy.tainted.putAll(this.tainted);
        return copy;
    }

    /**
     * Merges another state into this one. Used at CFG join points.
     * @return true if this state changed as a result of the merge.
     */
    public boolean merge(RegisterState other) {
        boolean changed = false;
        for (Map.Entry<Integer, String> entry : other.types.entrySet()) {
            int reg = entry.getKey();
            String otherType = entry.getValue();
            String thisType = types.get(reg);

            if (thisType == null) {
                types.put(reg, otherType);
                values.put(reg, other.values.get(reg));
                tainted.put(reg, other.tainted.get(reg));
                changed = true;
            } else if (!thisType.equals(otherType)) {
                // Type conflict at join point -> generalized to Object
                types.put(reg, "Ljava/lang/Object;");
                changed = true;
            }

            // Taint propagation: if any path is tainted, the result is tainted
            boolean otherTainted = other.isTainted(reg);
            if (otherTainted && !isTainted(reg)) {
                tainted.put(reg, true);
                changed = true;
            }
        }
        return changed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegisterState that = (RegisterState) o;
        return Objects.equals(values, that.values) &&
               Objects.equals(types, that.types) &&
               Objects.equals(tainted, that.tainted);
    }

    @Override
    public int hashCode() {
        return Objects.hash(values, types, tainted);
    }

    @Override
    public String toString() {
        return "Registers[types=" + types + ", tainted=" + tainted + "]";
    }
}
