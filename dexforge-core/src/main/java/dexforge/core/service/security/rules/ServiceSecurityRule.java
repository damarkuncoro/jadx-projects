package dexforge.core.service.security.rules;

import dexforge.core.parser.apk.ApkLoader;
import dexforge.core.parser.axml.service.manifest.ManifestAnalyzer;
import dexforge.core.service.security.model.VulnerabilityIssue;
import java.util.List;

/**
 * SOLID Rule to audit Android Services for security misconfigurations.
 */
public final class ServiceSecurityRule implements SecurityRule {

    @Override
    public void execute(ApkLoader loader, List<VulnerabilityIssue> issues) {
        ManifestAnalyzer manifestAnalyzer = loader.getManifestAnalyzer();
        if (manifestAnalyzer == null) return;

        for (ManifestAnalyzer.ComponentInfo service : manifestAnalyzer.getDetailedServices()) {
            // 1. Audit Exported Services
            if (service.isExported() && service.getPermission() == null) {
                String desc = service.getIntentFilters().isEmpty() ?
                    "Exported service without permission" :
                    "Service with intent-filter is exported without permission (implicitly accessible)";

                issues.add(new VulnerabilityIssue("Service Security",
                    desc + ": " + service.getName(),
                    VulnerabilityIssue.Severity.HIGH, service.getName()));
            }

            // 2. Audit Intent Filters for specific sensitive actions
            for (var filter : service.getIntentFilters()) {
                for (String action : filter.getActions()) {
                    if (action.endsWith("SEND") || action.endsWith("RECEIVE")) {
                        issues.add(new VulnerabilityIssue("Data Flow",
                            "Service handles potential data transfer action: " + action,
                            VulnerabilityIssue.Severity.INFO, service.getName()));
                    }
                    if (action.equals("android.intent.action.BOOT_COMPLETED")) {
                        issues.add(new VulnerabilityIssue("Persistence",
                            "Service requested to start at boot",
                            VulnerabilityIssue.Severity.LOW, service.getName()));
                    }
                }
            }
        }
    }
}
