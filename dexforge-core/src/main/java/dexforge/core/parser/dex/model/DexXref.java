package dexforge.core.parser.dex.model;

/**
 * Represents a cross-reference (usage) of a DEX item.
 */
public final class DexXref {
    private final DexClass clazz;
    private final DexEncodedMethod method;

    public DexXref(DexClass clazz, DexEncodedMethod method) {
        this.clazz = clazz;
        this.method = method;
    }

    public DexClass getDexClass() { return clazz; }
    public DexEncodedMethod getMethod() { return method; }

    @Override
    public String toString() {
        return clazz.getName() + " -> " + method.getMethodIndex();
    }
}
