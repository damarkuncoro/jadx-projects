package jadx.gui.buildstack.rules;

import java.util.List;

public class KotlinRuntimeRule extends BasePresenceRule {
	public KotlinRuntimeRule() {
		super(
				"Kotlin runtime",
				"MEDIUM",
				List.of("kotlin-tooling-metadata.json"),
				List.of("kotlin"));
	}
}
