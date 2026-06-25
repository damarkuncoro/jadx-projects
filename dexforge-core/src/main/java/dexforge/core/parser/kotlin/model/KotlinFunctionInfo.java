package dexforge.core.parser.kotlin.model;

/**
 * REUSEABLE: Represents metadata about a Kotlin function recovered from @Metadata annotation.
 */
public final class KotlinFunctionInfo {
    private final String name;
    private final boolean isInline;
    private final boolean isSuspend;

    public KotlinFunctionInfo(String name, boolean isInline, boolean isSuspend) {
        this.name = name;
        this.isInline = isInline;
        this.isSuspend = isSuspend;
    }

    public String getName() { return name; }
    public boolean isInline() { return isInline; }
    public boolean isSuspend() { return isSuspend; }
}
