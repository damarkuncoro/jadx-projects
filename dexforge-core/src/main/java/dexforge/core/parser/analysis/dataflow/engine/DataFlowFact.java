package dexforge.core.parser.analysis.dataflow.engine;

/**
 * REUSEABLE: Represents a piece of information being tracked in Data Flow Analysis.
 * Could be a constant value, a type, or a security tag (taint).
 */
public interface DataFlowFact<T extends DataFlowFact<T>> {
    /**
     * Merges this fact with another. Returns true if this fact was modified.
     * Essential for the Lattice 'Join' operation in DFA.
     */
    boolean merge(T other);

    /**
     * Creates a deep copy of this fact.
     */
    T copy();
}
