package jadx.gui.buildstack.rules;

import java.util.List;

public class TauriRule extends BasePresenceRule {
	public TauriRule() {
		super(
				"Tauri",
				"HIGH",
				List.of("assets/tauri.conf.json", "libtauri.so"),
				List.of());
	}
}
