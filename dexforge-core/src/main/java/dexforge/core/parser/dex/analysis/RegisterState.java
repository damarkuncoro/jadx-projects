package dexforge.core.parser.dex.analysis;

/**
 * Tracks the known value of a register at a specific point in time.
 */
public final class RegisterState {
    public enum Type { UNKNOWN, STRING, INTEGER, LONG, REFERENCE }

    public final Type type;
    public final Object value;

    public RegisterState(Type type, Object value) {
        this.type = type;
        this.value = value;
    }

    public static final RegisterState UNKNOWN = new RegisterState(Type.UNKNOWN, null);

    @Override
    public String toString() {
        return type == Type.UNKNOWN ? "?" : String.valueOf(value);
    }
}
