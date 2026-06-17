package jadx.gui.buildstack.rules;

import jadx.gui.buildstack.EvidenceUtils;
import jadx.gui.buildstack.FrameworkRule;
import jadx.gui.buildstack.RuleContext;

import java.util.List;

public class FlutterRule implements FrameworkRule {
    @Override
    public String getName() {
        return "Flutter";
    }

    @Override
    public String getConfidence() {
        return "HIGH";
    }

    @Override
    public boolean detect(RuleContext ctx) {
        return ctx.getResourceNames().stream().anyMatch(name -> name.startsWith("flutter_assets/"))
            || EvidenceUtils.containsResource(ctx.getResourceNames(), "libflutter.so");
    }

    @Override
    public List<String> getEvidence(RuleContext ctx) {
        return EvidenceUtils.matchingResources(
            ctx.getResourceNames(),
            name -> name.startsWith("flutter_assets/") || name.endsWith("/libflutter.so")
        );
    }
}
