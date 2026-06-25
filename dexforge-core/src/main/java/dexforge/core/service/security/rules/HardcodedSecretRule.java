package dexforge.core.service.security.rules;

import dexforge.core.parser.apk.ApkLoader;
import dexforge.core.service.security.model.VulnerabilityIssue;
import java.util.List;
import java.util.Map;

public final class HardcodedSecretRule implements SecurityRule {

    @Override
    public void execute(ApkLoader loader, List<VulnerabilityIssue> issues) {
        Map<String, List<String>> patterns = loader.getDiscoveredPatterns();

        if (patterns.containsKey("AWS_KEY") && !patterns.get("AWS_KEY").isEmpty()) {
            issues.add(new VulnerabilityIssue("Sensitive Data", "Hardcoded AWS Access Key found",
                       VulnerabilityIssue.Severity.HIGH, "DEX Strings"));
        }

        if (patterns.containsKey("GOOGLE_API_KEY") && !patterns.get("GOOGLE_API_KEY").isEmpty()) {
            issues.add(new VulnerabilityIssue("Sensitive Data", "Hardcoded Google API Key found",
                       VulnerabilityIssue.Severity.MEDIUM, "DEX Strings"));
        }
    }
}
