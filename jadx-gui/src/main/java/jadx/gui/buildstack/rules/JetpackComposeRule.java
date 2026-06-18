package jadx.gui.buildstack.rules;

public class JetpackComposeRule extends BaseLibraryAndClassRule {
	public JetpackComposeRule() {
		super(
				"Jetpack Compose",
				"HIGH",
				"androidx.compose.",
				"androidx/compose");
	}
}
