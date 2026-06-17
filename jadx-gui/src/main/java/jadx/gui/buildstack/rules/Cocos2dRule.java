package jadx.gui.buildstack.rules;

import java.util.List;

public class Cocos2dRule extends BasePresenceRule {
    public Cocos2dRule() {
        super(
            "Cocos2d",
            "HIGH",
            List.of("libcocos2d.so", "libcocos2djs.so"),
            List.of("org/cocos2dx", "org/cocos2d")
        );
    }
}
