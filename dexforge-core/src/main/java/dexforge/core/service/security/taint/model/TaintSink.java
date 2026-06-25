package dexforge.core.service.security.taint.model;

/**
 * REUSEABLE: Defines a dangerous destination for sensitive data.
 */
public final class TaintSink {
    private final String methodSignature;
    private final String label; // e.g., "HTTP_SEND", "LOG_INJECTION"

    public TaintSink(String methodSignature, String label) {
        this.methodSignature = methodSignature;
        this.label = label;
    }

    public String getMethodSignature() { return methodSignature; }
    public String getLabel() { return label; }
}
