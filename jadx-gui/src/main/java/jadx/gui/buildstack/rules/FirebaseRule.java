package jadx.gui.buildstack.rules;

import java.util.List;

import jadx.gui.buildstack.EvidenceUtils;
import jadx.gui.buildstack.FrameworkRule;
import jadx.gui.buildstack.RuleContext;

public class FirebaseRule implements FrameworkRule {
	@Override
	public String getName() {
		return "Firebase";
	}

	@Override
	public String getConfidence() {
		return "HIGH";
	}

	@Override
	public boolean detect(RuleContext ctx) {
		return ctx.getResourceNames().contains("google-services.json")
				|| EvidenceUtils.hasLibrary(ctx.getLibraryVersions(), "com.google.firebase")
				|| ctx.getClassNames().stream().anyMatch(cls -> cls.startsWith("com/google/firebase/"));
	}

	@Override
	public List<String> getEvidence(RuleContext ctx) {
		return EvidenceUtils.matchingFirebaseEvidence(
				ctx.getResourceNames(),
				ctx.getLibraryVersions(),
				ctx.getClassNames());
	}
}
