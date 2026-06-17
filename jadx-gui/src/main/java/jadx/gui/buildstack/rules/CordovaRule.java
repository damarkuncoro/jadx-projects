package jadx.gui.buildstack.rules;

import java.util.List;

public class CordovaRule extends BasePresenceRule {
    public CordovaRule() {
        super(
            "Cordova",
            "HIGH",
            List.of("assets/www/cordova.js"),
            List.of("org/apache/cordova/CordovaActivity")
        );
    }
}
