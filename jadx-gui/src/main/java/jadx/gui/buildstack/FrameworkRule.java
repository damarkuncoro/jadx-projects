package jadx.gui.buildstack;

import java.util.List;

/**
 * Interface untuk aturan deteksi framework.
 */
public interface FrameworkRule {
	String getName();

	String getConfidence();

	boolean detect(RuleContext ctx);

	List<String> getEvidence(RuleContext ctx);
}
