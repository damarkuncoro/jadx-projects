package dexforge.core.parser.analysis.dataflow.model;

import dexforge.core.parser.analysis.dataflow.engine.DalvikRegisterFact;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * REUSEABLE: Represents a summary of a method's effect on data flow and semantics.
 * Enhanced to track specific semantic events (like Decryption).
 */
public final class MethodSummary {
    private final String methodSignature;
    private final DalvikRegisterFact returnFact;
    private final Set<String> semanticEvents = new HashSet<>();
    private final Map<Integer, Boolean> parameterTaint = new HashMap<>();

    public MethodSummary(String signature, DalvikRegisterFact returnFact) {
        this.methodSignature = signature;
        this.returnFact = returnFact;
    }

    public String getMethodSignature() { return methodSignature; }
    public DalvikRegisterFact getReturnFact() { return returnFact; }

    public void addSemanticEvent(String event) {
        semanticEvents.add(event);
    }

    public boolean hasSemanticEvent(String event) {
        return semanticEvents.contains(event);
    }

    public Set<String> getSemanticEvents() {
        return semanticEvents;
    }

    public void setParamTainted(int paramIdx, boolean tainted) {
        parameterTaint.put(paramIdx, tainted);
    }

    public boolean isParamTainted(int paramIdx) {
        return parameterTaint.getOrDefault(paramIdx, false);
    }
}
