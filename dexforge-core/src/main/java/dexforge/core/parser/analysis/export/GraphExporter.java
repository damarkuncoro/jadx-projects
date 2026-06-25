package dexforge.core.parser.analysis.export;

import dexforge.core.parser.analysis.callgraph.model.CallGraphNode;
import dexforge.core.parser.analysis.cfg.ControlFlowGraph;
import dexforge.core.parser.analysis.cfg.model.CfgBlock;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

/**
 * Utility to export graphs (Call Graph, CFG) to DOT format for visualization.
 */
public final class GraphExporter {

    public String exportCallGraph(Map<String, CallGraphNode> nodes) {
        StringBuilder sb = new StringBuilder();
        sb.append("digraph CallGraph {\n");
        sb.append("  node [shape=box, fontname=\"Courier\"];\n");

        Set<String> processedEdges = new HashSet<>();

        for (CallGraphNode node : nodes.values()) {
            String caller = sanitize(node.getSignature());
            // Highlight hot nodes (many callers)
            String color = node.getFanIn() > 10 ? "red" : (node.getFanIn() > 3 ? "orange" : "black");
            sb.append(String.format("  \"%s\" [label=\"%s\", color=\"%s\"];\n", caller, node.getSignature(), color));

            for (CallGraphNode callee : node.getCallees()) {
                String calleeSig = sanitize(callee.getSignature());
                String edge = caller + " -> " + calleeSig;
                if (processedEdges.add(edge)) {
                    sb.append(String.format("  \"%s\" -> \"%s\";\n", caller, calleeSig));
                }
            }
        }

        sb.append("}\n");
        return sb.toString();
    }

    public String exportCfg(ControlFlowGraph cfg, String methodName) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("digraph CFG_%s {\n", sanitize(methodName)));
        sb.append("  node [shape=record, fontname=\"Courier\"];\n");

        for (CfgBlock block : cfg.getBlocks()) {
            StringBuilder label = new StringBuilder();
            label.append(String.format("{ Block %d | ", block.getId()));

            block.getInstructions().forEach(insn -> {
                label.append(sanitize(insn.toString())).append("\\l");
            });
            label.append("}");

            sb.append(String.format("  block_%d [label=\"%s\"];\n", block.getId(), label.toString()));

            for (CfgBlock successor : block.getSuccessors()) {
                sb.append(String.format("  block_%d -> block_%d;\n", block.getId(), successor.getId()));
            }
        }

        sb.append("}\n");
        return sb.toString();
    }

    private String sanitize(String input) {
        if (input == null) return "";
        return input.replace("\"", "\\\"").replace("\n", " ").replace(";", "");
    }
}
