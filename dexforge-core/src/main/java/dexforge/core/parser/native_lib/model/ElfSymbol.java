package dexforge.core.parser.native_lib.model;

public final class ElfSymbol {
    private final String name;
    private final long address;
    private final boolean isExported;

    public ElfSymbol(String name, long address, boolean isExported) {
        this.name = name;
        this.address = address;
        this.isExported = isExported;
    }

    public String getName() { return name; }
    public long getAddress() { return address; }
    public boolean isExported() { return isExported; }

    public boolean isJniMethod() {
        return name != null && name.startsWith("Java_");
    }
}
