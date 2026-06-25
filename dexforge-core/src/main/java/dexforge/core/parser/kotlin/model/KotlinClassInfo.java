package dexforge.core.parser.kotlin.model;

import java.util.ArrayList;
import java.util.List;

public final class KotlinClassInfo {
    private String name;
    private boolean isInterface;
    private boolean isCompanion;
    private final List<String> functions = new ArrayList<>();
    private final List<String> properties = new ArrayList<>();

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public boolean isInterface() { return isInterface; }
    public void setInterface(boolean isInterface) { this.isInterface = isInterface; }
    public boolean isCompanion() { return isCompanion; }
    public void setCompanion(boolean isCompanion) { this.isCompanion = isCompanion; }
    public List<String> getFunctions() { return functions; }
    public List<String> getProperties() { return properties; }
}
