package jadx.gui.buildstack.rules;

import java.util.List;

public class XamarinRule extends BasePresenceRule {
	public XamarinRule() {
		super(
				"Xamarin",
				"HIGH",
				List.of("libmonodroid.so"),
				List.of("mono/android/Runtime"));
	}
}
