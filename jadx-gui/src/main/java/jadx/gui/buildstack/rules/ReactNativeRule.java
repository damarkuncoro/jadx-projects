package jadx.gui.buildstack.rules;

import java.util.List;

public class ReactNativeRule extends BasePresenceRule {
	public ReactNativeRule() {
		super(
				"React Native",
				"HIGH",
				List.of("assets/index.android.bundle"),
				List.of("com/facebook/react/ReactActivity", "com/facebook/react/ReactNativeHost"));
	}
}
