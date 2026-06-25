package dexforge.core.service.security.rules;

import dexforge.core.parser.apk.ApkLoader;
import dexforge.core.service.security.model.VulnerabilityIssue;
import java.util.List;

/**
 * Interface for a standalone security rule.
 * Promotes SOLID (Open-Closed Principle).
 */
public interface SecurityRule {
    void execute(ApkLoader loader, List<VulnerabilityIssue> issues);
}
