package dexforge.core.service.security.rules;

import dexforge.core.parser.apk.ApkLoader;
import dexforge.core.parser.axml.service.manifest.ManifestAnalyzer;
import dexforge.core.service.security.model.VulnerabilityIssue;
import java.util.List;

public final class ManifestSecurityRule implements SecurityRule {

    @Override
    public void execute(ApkLoader loader, List<VulnerabilityIssue> issues) {
        ManifestAnalyzer manifestAnalyzer = loader.getManifestAnalyzer();
        if (manifestAnalyzer == null) return;

        if (manifestAnalyzer.isDebuggable()) {
            issues.add(new VulnerabilityIssue("Config", "Application is debuggable",
                       VulnerabilityIssue.Severity.HIGH, "AndroidManifest.xml"));
        }

        if (manifestAnalyzer.isAllowBackupEnabled()) {
            issues.add(new VulnerabilityIssue("Data Safety", "Backup is enabled (allowBackup=true)",
                       VulnerabilityIssue.Severity.LOW, "AndroidManifest.xml"));
        }
    }
}
