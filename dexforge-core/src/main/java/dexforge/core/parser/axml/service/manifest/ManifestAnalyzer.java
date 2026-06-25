package dexforge.core.parser.axml.service.manifest;

import dexforge.core.parser.axml.model.AxmlAttribute;
import dexforge.core.parser.axml.model.AxmlNode;
import dexforge.core.model.common.AndroidComponent;
import java.util.ArrayList;
import java.util.List;

/**
 * High-level analyzer for AndroidManifest.xml.
 * Follows SOLID principles by using a common component model.
 */
public final class ManifestAnalyzer {
    private final AxmlNode root;

    public ManifestAnalyzer(AxmlNode root) {
        this.root = root;
    }

    public List<String> getActivities() { return findComponentNames("activity"); }
    public List<String> getServices() { return findComponentNames("service"); }
    public List<String> getReceivers() { return findComponentNames("receiver"); }

    public List<ComponentInfo> getDetailedActivities() { return findDetailedComponents("activity"); }
    public List<ComponentInfo> getDetailedServices() { return findDetailedComponents("service"); }
    public List<ComponentInfo> getDetailedReceivers() { return findDetailedComponents("receiver"); }

    public boolean isDebuggable() {
        AxmlNode application = findChild(root, "application");
        if (application != null) {
            String val = getAttributeValue(application, "debuggable");
            return "true".equals(val) || "1".equals(val) || "-1".equals(val);
        }
        return false;
    }

    public boolean isCleartextTrafficPermitted() {
        AxmlNode application = findChild(root, "application");
        if (application != null) {
            String val = getAttributeValue(application, "usesCleartextTraffic");
            return !"false".equals(val) && !"0".equals(val);
        }
        return true;
    }

    public boolean isAllowBackupEnabled() {
        AxmlNode application = findChild(root, "application");
        if (application != null) {
            String val = getAttributeValue(application, "allowBackup");
            return !"false".equals(val) && !"0".equals(val);
        }
        return true;
    }

    private List<String> findComponentNames(String tagName) {
        List<String> names = new ArrayList<>();
        for (ComponentInfo info : findDetailedComponents(tagName)) {
            names.add(info.getName());
        }
        return names;
    }

    private List<ComponentInfo> findDetailedComponents(String tagName) {
        List<ComponentInfo> components = new ArrayList<>();
        AxmlNode application = findChild(root, "application");
        if (application != null) {
            for (AxmlNode child : application.getChildren()) {
                if (tagName.equals(child.getName())) {
                    String name = getAttributeValue(child, "name");
                    String permission = getAttributeValue(child, "permission");

                    // Android rule: if it has intent-filters and no 'exported' attr, it defaults to exported=true
                    String exportedStr = getAttributeValue(child, "exported");
                    boolean hasIntentFilters = !findChildren(child, "intent-filter").isEmpty();
                    boolean exported = "true".equals(exportedStr) || (exportedStr == null && hasIntentFilters);

                    if (name != null) {
                        ComponentInfo info = new ComponentInfo(name, exported, permission);
                        extractIntentFilters(child, info);
                        components.add(info);
                    }
                }
            }
        }
        return components;
    }

    private void extractIntentFilters(AxmlNode componentNode, ComponentInfo info) {
        for (AxmlNode filterNode : findChildren(componentNode, "intent-filter")) {
            AndroidComponent.IntentFilter filter = new AndroidComponent.IntentFilter();
            for (AxmlNode actionNode : findChildren(filterNode, "action")) {
                String action = getAttributeValue(actionNode, "name");
                if (action != null) filter.addAction(action);
            }
            info.getIntentFilters().add(filter);
        }
    }

    public static final class ComponentInfo extends AndroidComponent {
        public ComponentInfo(String name, boolean exported, String permission) {
            super(name, exported, permission);
        }
    }

    private AxmlNode findChild(AxmlNode parent, String name) {
        for (AxmlNode child : parent.getChildren()) {
            if (name.equals(child.getName())) return child;
        }
        return null;
    }

    private List<AxmlNode> findChildren(AxmlNode parent, String name) {
        List<AxmlNode> children = new ArrayList<>();
        for (AxmlNode child : parent.getChildren()) {
            if (name.equals(child.getName())) children.add(child);
        }
        return children;
    }

    private String getAttributeValue(AxmlNode node, String attrName) {
        for (AxmlAttribute attr : node.getAttributes()) {
            // Support both direct name and android:name pattern
            if (attrName.equals(attr.getName()) || ("android:" + attrName).equals(attr.getName())) {
                return attr.getValue();
            }
        }
        return null;
    }
}
