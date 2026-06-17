package jadx.gui.buildstack.rules;

import jadx.gui.buildstack.EvidenceUtils;
import jadx.gui.buildstack.FrameworkRule;
import jadx.gui.buildstack.RuleContext;

import java.util.List;

public class NativeAndroidRule implements FrameworkRule {
    @Override
    public String getName() {
        return "Native Android";
    }

    @Override
    public String getConfidence() {
        return "HIGH";
    }

    @Override
    public boolean detect(RuleContext ctx) {
        return EvidenceUtils.containsResource(ctx.getResourceNames(), "AndroidManifest.xml");
    }

    @Override
    public List<String> getEvidence(RuleContext ctx) {
        return EvidenceUtils.matchingEvidence(
            ctx.getResourceNames(),
            ctx.getClassNames(),
            List.of("AndroidManifest.xml"),
            List.of()
        );
    }
}
