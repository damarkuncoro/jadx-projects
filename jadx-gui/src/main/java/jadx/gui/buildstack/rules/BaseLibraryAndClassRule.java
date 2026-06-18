package jadx.gui.buildstack.rules;

import java.util.List;

import jadx.gui.buildstack.EvidenceUtils;
import jadx.gui.buildstack.FrameworkRule;
import jadx.gui.buildstack.RuleContext;

public abstract class BaseLibraryAndClassRule implements FrameworkRule {
	private final String name;
	private final String confidence;
	private final String libraryPrefix;
	private final String classPrefix;

	protected BaseLibraryAndClassRule(String name, String confidence, String libraryPrefix, String classPrefix) {
		this.name = name;
		this.confidence = confidence;
		this.libraryPrefix = libraryPrefix;
		this.classPrefix = classPrefix;
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
		return EvidenceUtils.hasLibrary(ctx.getLibraryVersions(), libraryPrefix)
				|| ctx.getClassNames().stream().anyMatch(cls -> cls.startsWith(classPrefix + "/"));
	}

	@Override
	public List<String> getEvidence(RuleContext ctx) {
		return EvidenceUtils.matchingLibraryAndClassEvidence(
				ctx.getLibraryVersions(),
				ctx.getClassNames(),
				libraryPrefix,
				classPrefix);
	}
}
