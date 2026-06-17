package jadx.gui.buildstack.rules;

import java.util.List;

public class CapacitorRule extends BasePresenceRule {
    public CapacitorRule() {
        super(
            "Capacitor",
            "HIGH",
            List.of("assets/capacitor.config.json"),
            List.of("com/getcapacitor/BridgeActivity")
        );
    }
}
