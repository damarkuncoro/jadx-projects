package jadx.gui.buildstack.rules;

import java.util.List;

import jadx.gui.buildstack.EvidenceUtils;
import jadx.gui.buildstack.FrameworkRule;
import jadx.gui.buildstack.RuleContext;

public class R8ProGuardRule implements FrameworkRule {
	@Override
	public String getName() {
		return "R8 / ProGuard";
	}

	@Override
	public String getConfidence() {
		return "MEDIUM";
	}

	@Override
	public boolean detect(RuleContext ctx) {
		return ctx.getResourceNames().stream()
				.anyMatch(name -> name.startsWith("META-INF/proguard/") || name.endsWith("proguard-project.txt"))
				|| ctx.getClassNames().stream().anyMatch(cls -> cls.startsWith("com/android/tools/r8/"));
	}

	@Override
	public List<String> getEvidence(RuleContext ctx) {
		return EvidenceUtils.matchingR8Evidence(ctx.getResourceNames(), ctx.getClassNames());
	}
}
