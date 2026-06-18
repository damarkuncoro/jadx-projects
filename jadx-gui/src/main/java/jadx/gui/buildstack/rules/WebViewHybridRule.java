package jadx.gui.buildstack.rules;

import java.util.List;

import jadx.gui.buildstack.EvidenceUtils;
import jadx.gui.buildstack.FrameworkRule;
import jadx.gui.buildstack.RuleContext;

public class WebViewHybridRule implements FrameworkRule {
	@Override
	public String getName() {
		return "WebView / Hybrid";
	}

	@Override
	public String getConfidence() {
		return "MEDIUM";
	}

	@Override
	public boolean detect(RuleContext ctx) {
		return ctx.getResourceNames().stream().anyMatch(name -> name.startsWith("assets/www/") || name.startsWith("assets/public/"))
				|| ctx.getClassNames().stream().anyMatch(cls -> cls.endsWith("/WebViewActivity") || cls.endsWith("/WebViewFragment"));
	}

	@Override
	public List<String> getEvidence(RuleContext ctx) {
		return EvidenceUtils.matchingWebViewEvidence(ctx.getResourceNames(), ctx.getClassNames());
	}
}
