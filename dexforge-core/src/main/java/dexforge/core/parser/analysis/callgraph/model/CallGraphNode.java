package dexforge.core.parser.analysis.callgraph.model;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a method in the Call Graph.
 */
public final class CallGraphNode {
    private final String signature;
    private final Set<CallGraphNode> callees = new HashSet<>();
    private final Set<CallGraphNode> callers = new HashSet<>();

    public CallGraphNode(String signature) {
        this.signature = signature;
    }

    public String getSignature() { return signature; }
    public Set<CallGraphNode> getCallees() { return callees; }
    public Set<CallGraphNode> getCallers() { return callers; }

    public int getFanIn() {
        return callers.size();
    }

    public void addCall(CallGraphNode callee) {
        callees.add(callee);
        callee.callers.add(this);
    }
}
