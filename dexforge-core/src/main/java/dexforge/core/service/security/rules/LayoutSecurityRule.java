package dexforge.core.service.security.rules;

import dexforge.core.parser.apk.ApkLoader;
import dexforge.core.parser.axml.model.AxmlAttribute;
import dexforge.core.parser.axml.model.AxmlNode;
import dexforge.core.service.security.model.VulnerabilityIssue;
import java.util.List;

/**
 * SOLID Rule to audit layout files for security issues.
 */
public final class LayoutSecurityRule implements SecurityRule {

    @Override
    public void execute(ApkLoader loader, List<VulnerabilityIssue> issues) {
        for (AxmlNode root : loader.getLayouts()) {
            auditNode(root, issues);
        }
    }

    private void auditNode(AxmlNode node, List<VulnerabilityIssue> issues) {
        for (AxmlAttribute attr : node.getAttributes()) {
            if ("onClick".equals(attr.getName())) {
                String val = attr.getValue();
                if (val != null && (val.contains("decrypt") || val.contains("exec"))) {
                    issues.add(new VulnerabilityIssue("UI Security",
                        "Suspicious onClick handler: " + val,
                        VulnerabilityIssue.Severity.MEDIUM, "Layout View: " + node.getName()));
                }
            }
        }
        for (AxmlNode child : node.getChildren()) {
            auditNode(child, issues);
        }
    }
}
