package jadx.gui.device.debugger;

import jadx.gui.ui.panel.JDebuggerPanel;

public class ThreadBoxElement implements JDebuggerPanel.IListElement {
    private long threadID;
    private String name;

    public ThreadBoxElement(long threadID) {
        this.threadID = threadID;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getThreadID() {
        return threadID;
    }

    @Override
    public String toString() {
        if (name == null) {
            return "thread id: " + threadID;
        }
        return "thread id: " + threadID + " name:" + name;
    }

    @Override
    public void onSelected() {
    }
}
