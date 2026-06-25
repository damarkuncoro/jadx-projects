package dexforge.core.parser.dex.model;

/**
 * Utility to convert DEX access flags to human-readable strings.
 */
public final class DexAccessFlags {
    public static final int ACC_PUBLIC = 0x1;
    public static final int ACC_PRIVATE = 0x2;
    public static final int ACC_PROTECTED = 0x4;
    public static final int ACC_STATIC = 0x8;
    public static final int ACC_FINAL = 0x10;
    public static final int ACC_INTERFACE = 0x200;
    public static final int ACC_ABSTRACT = 0x400;
    public static final int ACC_SYNCHRONIZED = 0x20;
    public static final int ACC_NATIVE = 0x100;

    public static String format(int flags) {
        StringBuilder sb = new StringBuilder();
        if ((flags & ACC_PUBLIC) != 0) sb.append("public ");
        if ((flags & ACC_PRIVATE) != 0) sb.append("private ");
        if ((flags & ACC_PROTECTED) != 0) sb.append("protected ");
        if ((flags & ACC_STATIC) != 0) sb.append("static ");
        if ((flags & ACC_FINAL) != 0) sb.append("final ");
        if ((flags & ACC_SYNCHRONIZED) != 0) sb.append("synchronized ");
        if ((flags & ACC_NATIVE) != 0) sb.append("native ");
        if ((flags & ACC_ABSTRACT) != 0) sb.append("abstract ");
        if ((flags & ACC_INTERFACE) != 0) sb.append("interface ");
        return sb.toString().trim();
    }
}
