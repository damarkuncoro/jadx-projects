package dexforge.core.service.security.taint.model;

/**
 * REUSEABLE: Defines a source of sensitive data (PII).
 */
public final class TaintSource {
    private final String methodSignature;
    private final String label; // e.g., "LOCATION", "CONTACTS", "IMEI"

    public TaintSource(String methodSignature, String label) {
        this.methodSignature = methodSignature;
        this.label = label;
    }

    public String getMethodSignature() { return methodSignature; }
    public String getLabel() { return label; }
}
