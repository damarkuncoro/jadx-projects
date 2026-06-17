package jadx.gui.device.debugger;

import jadx.gui.ui.panel.JDebuggerPanel;

public abstract class RuntimeValueTreeNode extends JDebuggerPanel.ValueTreeNode {
    private static final long serialVersionUID = -1111111202103260222L;
    private long typeID;

    @Override
    public JDebuggerPanel.ValueTreeNode updateTypeID(long id) {
        this.typeID = id;
        return this;
    }

    @Override
    public long getTypeID() {
        return this.typeID;
    }

    public abstract SmaliDebugger.RuntimeValue getRuntimeValue();

    public abstract boolean isAbsoluteType();
}
