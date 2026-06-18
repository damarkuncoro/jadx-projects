package jadx.gui.buildstack.rules;

import java.util.List;

public class UnrealEngineRule extends BasePresenceRule {
	public UnrealEngineRule() {
		super(
				"Unreal Engine",
				"HIGH",
				List.of("libUE4.so", "libUnreal.so"),
				List.of("com/epicgames/ue4", "com/epicgames/unreal"));
	}
}
