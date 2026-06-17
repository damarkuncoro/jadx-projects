package jadx.gui.buildstack.rules;

import jadx.gui.buildstack.EvidenceUtils;
import jadx.gui.buildstack.FrameworkRule;
import jadx.gui.buildstack.RuleContext;

import java.util.List;

public class DaggerHiltRule implements FrameworkRule {
    @Override
    public String getName() {
        return "Dagger / Hilt";
    }

    @Override
    public String getConfidence() {
        return "HIGH";
    }

    @Override
    public boolean detect(RuleContext ctx) {
        return EvidenceUtils.hasLibrary(ctx.getLibraryVersions(), "com.google.dagger")
            || ctx.getClassNames().stream().anyMatch(cls -> cls.startsWith("dagger/") || cls.startsWith("javax/inject/"));
    }

    @Override
    public List<String> getEvidence(RuleContext ctx) {
        return EvidenceUtils.matchingDaggerEvidence(ctx.getLibraryVersions(), ctx.getClassNames());
    }
}
