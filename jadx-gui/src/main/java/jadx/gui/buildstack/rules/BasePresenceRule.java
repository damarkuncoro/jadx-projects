package jadx.gui.buildstack.rules;

import java.util.List;

import jadx.gui.buildstack.EvidenceUtils;
import jadx.gui.buildstack.FrameworkRule;
import jadx.gui.buildstack.RuleContext;

public abstract class BasePresenceRule implements FrameworkRule {
	private final String name;
	private final String confidence;
	private final List<String> resourceEvidence;
	private final List<String> classEvidence;

	protected BasePresenceRule(String name, String confidence, List<String> resourceEvidence, List<String> classEvidence) {
		this.name = name;
		this.confidence = confidence;
		this.resourceEvidence = resourceEvidence;
		this.classEvidence = classEvidence;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getConfidence() {
		return confidence;
	}

	@Override
	public boolean detect(RuleContext ctx) {
		for (String r : resourceEvidence) {
			if (r.contains("/") && ctx.getResourceNames().contains(r)) {
				return true;
			}
			if (!r.contains("/") && EvidenceUtils.containsResource(ctx.getResourceNames(), r)) {
				return true;
			}
		}
		for (String c : classEvidence) {
			if (ctx.getClassNames().contains(c)) {
				return true;
			}
			if (ctx.getClassNames().stream().anyMatch(clsName -> clsName.startsWith(c + "/"))) {
				return true;
			}
		}
		return false;
	}

	@Override
	public List<String> getEvidence(RuleContext ctx) {
		return EvidenceUtils.matchingEvidence(ctx.getResourceNames(), ctx.getClassNames(), resourceEvidence, classEvidence);
	}
}
