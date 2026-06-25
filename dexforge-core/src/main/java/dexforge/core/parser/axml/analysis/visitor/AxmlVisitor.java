package dexforge.core.parser.axml.analysis.visitor;

import dexforge.core.parser.axml.model.AxmlNode;

/**
 * REUSEABLE Visitor pattern for AXML node traversal.
 * SOLID: Decouples traversal from specific node processing.
 */
public interface AxmlVisitor {
    void visit(AxmlNode node);
}
