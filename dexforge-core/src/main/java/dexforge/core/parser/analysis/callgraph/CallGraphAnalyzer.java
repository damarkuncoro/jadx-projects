package dexforge.core.parser.analysis.callgraph;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dexforge.core.parser.analysis.callgraph.model.CallGraphNode;
import dexforge.core.parser.dex.model.DexClass;
import dexforge.core.parser.dex.model.DexEncodedMethod;
import dexforge.core.parser.dex.service.DexFastIndexer;

/**
 * Analyzes method calls across the entire DEX file to build a Global Call Graph.
 */
public final class CallGraphAnalyzer {
    private final DexFastIndexer indexer;
    private final Map<String, CallGraphNode> nodes = new HashMap<>();

    public CallGraphAnalyzer(DexFastIndexer indexer) {
        this.indexer = indexer;
    }

    public Map<String, CallGraphNode> build() {
        List<DexClass> classes = indexer.getClasses();

        // 1. Create nodes for all methods
        for (DexClass clazz : classes) {
            indexer.fillClassData(clazz);
            if (clazz.getClassData() != null) {
                registerMethods(clazz.getClassData().directMethods);
                registerMethods(clazz.getClassData().virtualMethods);
            }
        }

        // 2. Connect nodes by analyzing instructions
        for (DexClass clazz : classes) {
            if (clazz.getClassData() != null) {
                connectCalls(clazz.getClassData().directMethods);
                connectCalls(clazz.getClassData().virtualMethods);
            }
        }

        return nodes;
    }

    private void registerMethods(List<DexEncodedMethod> methods) {
        for (DexEncodedMethod m : methods) {
            String sig = indexer.getMethodPool().getMethodSignature(m.getMethodIndex());
            nodes.putIfAbsent(sig, new CallGraphNode(sig));
        }
    }

    private void connectCalls(List<DexEncodedMethod> methods) {
        for (DexEncodedMethod m : methods) {
            String callerSig = indexer.getMethodPool().getMethodSignature(m.getMethodIndex());
            CallGraphNode callerNode = nodes.get(callerSig);

            List<String> callees = indexer.getMethodCallsInMethod(m);
            for (String calleeSig : callees) {
                CallGraphNode calleeNode = nodes.get(calleeSig);
                if (calleeNode == null) {
                    // Method might be in Android SDK or another DEX
                    calleeNode = new CallGraphNode(calleeSig);
                    nodes.put(calleeSig, calleeNode);
                }
                callerNode.addCall(calleeNode);
            }
        }
    }
}
