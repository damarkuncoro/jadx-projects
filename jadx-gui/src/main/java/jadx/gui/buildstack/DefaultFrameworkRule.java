package jadx.gui.buildstack;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Implementasi default dari FrameworkRule.
 */
public class DefaultFrameworkRule implements FrameworkRule {
	private final String name;
	private final String confidence;
	private final Predicate<RuleContext> detectFunc;
	private final Function<RuleContext, List<String>> evidenceFunc;

	public DefaultFrameworkRule(String name, String confidence, Predicate<RuleContext> detectFunc,
			Function<RuleContext, List<String>> evidenceFunc) {
		this.name = name;
		this.confidence = confidence;
		this.detectFunc = detectFunc;
		this.evidenceFunc = evidenceFunc;
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
		return detectFunc.test(ctx);
	}

	@Override
	public List<String> getEvidence(RuleContext ctx) {
		return evidenceFunc.apply(ctx);
	}
}
