package jadx.gui.buildstack.rules;

import java.util.List;

public class AndroidXJetpackRule extends BasePresenceRule {
	public AndroidXJetpackRule() {
		super(
				"AndroidX / Jetpack",
				"HIGH",
				List.of("META-INF/androidx.core_core-ktx.version"),
				List.of("androidx"));
	}
}
