package jadx.gui.device.debugger;

import jadx.gui.device.debugger.smali.Smali;
import jadx.gui.treemodel.JClass;

public class DebuggerState {
    JClass clsNode;
    String mthFullID;
    Smali smali;
    FrameNode frame;
    RegisterObserver regAdapter;

    public void reset() {
        frame = null;
        smali = null;
        clsNode = null;
        regAdapter = null;
        mthFullID = "";
    }
}
