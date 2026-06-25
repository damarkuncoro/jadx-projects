package dexforge.core.model.common;

import java.util.ArrayList;
import java.util.List;

/**
 * REUSEABLE: Common interface or base class for all Android components (Activity, Service, etc.)
 */
public abstract class AndroidComponent {
    protected final String name;
    protected final boolean exported;
    protected final String permission;
    protected final List<IntentFilter> intentFilters = new ArrayList<>();

    protected AndroidComponent(String name, boolean exported, String permission) {
        this.name = name;
        this.exported = exported;
        this.permission = permission;
    }

    public String getName() { return name; }
    public boolean isExported() { return exported; }
    public String getPermission() { return permission; }
    public List<IntentFilter> getIntentFilters() { return intentFilters; }

    public static final class IntentFilter {
        private final List<String> actions = new ArrayList<>();

        public List<String> getActions() { return actions; }
        public void addAction(String action) { actions.add(action); }
    }
}
