package dexforge.core.parser.analysis.dataflow.engine;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * REUSEABLE: Stores the state of all registers for a single point in Dalvik execution.
 * Enhanced to support multiple taint categories.
 */
public final class DalvikRegisterFact implements DataFlowFact<DalvikRegisterFact> {
    private final Map<Integer, RegisterValue> registers = new HashMap<>();

    public static final class RegisterValue {
        public final Object constant;
        public final String type;
        public final Set<String> taintLabels;

        public RegisterValue(Object constant, String type, Set<String> taintLabels) {
            this.constant = constant;
            this.type = type;
            this.taintLabels = taintLabels != null ? new HashSet<>(taintLabels) : new HashSet<>();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RegisterValue that = (RegisterValue) o;
            return Objects.equals(constant, that.constant) &&
                   Objects.equals(type, that.type) &&
                   Objects.equals(taintLabels, that.taintLabels);
        }

        @Override
        public int hashCode() {
            return Objects.hash(constant, type, taintLabels);
        }
    }

    public void set(int reg, Object constant, String type, Set<String> taintLabels) {
        registers.put(reg, new RegisterValue(constant, type, taintLabels));
    }

    public String getType(int reg) {
        RegisterValue val = registers.get(reg);
        return val != null ? val.type : null;
    }

    public Object getValue(int reg) {
        RegisterValue val = registers.get(reg);
        return val != null ? val.constant : null;
    }

    public boolean isTainted(int reg) {
        RegisterValue val = registers.get(reg);
        return val != null && !val.taintLabels.isEmpty();
    }

    public Set<String> getTaintLabels(int reg) {
        RegisterValue val = registers.get(reg);
        return val != null ? val.taintLabels : new HashSet<>();
    }

    @Override
    public boolean merge(DalvikRegisterFact other) {
        boolean changed = false;
        for (Map.Entry<Integer, RegisterValue> entry : other.registers.entrySet()) {
            RegisterValue thisVal = registers.get(entry.getKey());
            RegisterValue otherVal = entry.getValue();

            if (thisVal == null) {
                registers.put(entry.getKey(), otherVal);
                changed = true;
            } else {
                // Taint propagation: Merge all labels from both paths
                Set<String> mergedLabels = new HashSet<>(thisVal.taintLabels);
                if (mergedLabels.addAll(otherVal.taintLabels)) {
                    changed = true;
                }

                String mergedType = Objects.equals(thisVal.type, otherVal.type) ? thisVal.type : "Ljava/lang/Object;";
                Object mergedConst = Objects.equals(thisVal.constant, otherVal.constant) ? thisVal.constant : null;

                RegisterValue newVal = new RegisterValue(mergedConst, mergedType, mergedLabels);
                if (!newVal.equals(thisVal)) {
                    registers.put(entry.getKey(), newVal);
                    changed = true;
                }
            }
        }
        return changed;
    }

    @Override
    public DalvikRegisterFact copy() {
        DalvikRegisterFact copy = new DalvikRegisterFact();
        for (Map.Entry<Integer, RegisterValue> entry : registers.entrySet()) {
            copy.registers.put(entry.getKey(), new RegisterValue(entry.getValue().constant, entry.getValue().type, entry.getValue().taintLabels));
        }
        return copy;
    }
}
