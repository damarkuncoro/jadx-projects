package jadx.gui.buildstack.rules;

import java.util.List;

public class UnityRule extends BasePresenceRule {
    public UnityRule() {
        super(
            "Unity",
            "HIGH",
            List.of("libunity.so"),
            List.of("com/unity3d/player/UnityPlayerActivity")
        );
    }
}
