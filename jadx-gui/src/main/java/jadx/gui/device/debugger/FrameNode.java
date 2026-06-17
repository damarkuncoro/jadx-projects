package jadx.gui.device.debugger;

import jadx.core.utils.StringUtils;
import jadx.gui.device.debugger.smali.Smali;
import jadx.gui.treemodel.JClass;
import jadx.gui.ui.panel.JDebuggerPanel;

import java.util.Collections;
import java.util.List;

public class FrameNode implements JDebuggerPanel.IListElement {
    private static final int DEFAULT_CACHE_SIZE = 512;

    private final JDebuggerPanel debuggerPanel;
    private SmaliDebugger.Frame frame;
    private final long threadID;
    private String clsSig;
    private String mthSig;
    private StringBuilder cache;
    private long codeOffset = -1;
    private List<RegTreeNode> regNodes;
    private List<FieldTreeNode> thisNodes;
    private long thisID;

    public FrameNode(JDebuggerPanel debuggerPanel, long threadID, SmaliDebugger.Frame frame) {
        this.debuggerPanel = debuggerPanel;
        cache = new StringBuilder(DEFAULT_CACHE_SIZE);
        this.frame = frame;
        this.threadID = threadID;
        regNodes = Collections.emptyList();
        thisNodes = Collections.emptyList();
    }

    public SmaliDebugger.Frame getFrame() {
        return frame;
    }

    public void setFrame(SmaliDebugger.Frame frame) {
        this.frame = frame;
    }

    public long getClsID() {
        return frame.getClassID();
    }

    public long getMthID() {
        return frame.getMethodID();
    }

    public long getThreadID() {
        return threadID;
    }

    public long getThisID() {
        return thisID;
    }

    public void setThisID(long thisID) {
        this.thisID = thisID;
    }

    public void setSignatures(String clsSig, String mthSig) {
        this.clsSig = clsSig;
        this.mthSig = mthSig;
        resetCache();
    }

    public String getClsSig() {
        return clsSig;
    }

    public String getMthSig() {
        return mthSig;
    }

    public void updateCodeOffset(long codeOffset) {
        this.codeOffset = codeOffset;
        if (this.codeOffset > -1) {
            resetCache();
        }
    }

    public long getCodeOffset() {
        return codeOffset == -1 ? frame.getCodeIndex() : codeOffset;
    }

    public void setRegNodes(java.util.List<RegTreeNode> regNodes) {
        this.regNodes = regNodes;
    }

    public java.util.List<RegTreeNode> getRegNodes() {
        return regNodes;
    }

    public java.util.List<FieldTreeNode> getFieldNodes() {
        return thisNodes;
    }

    public void setFieldNodes(java.util.List<FieldTreeNode> thisNodes) {
        this.thisNodes = thisNodes;
    }

    @Override
    public void onSelected() {
        if (clsSig != null) {
            JClass cls = DbgUtils.getTopClassBySig(clsSig, debuggerPanel.getMainWindow());
            if (cls != null) {
                Smali smali = DbgUtils.getSmali(cls.getCls().getClassNode());
                if (smali != null) {
                    int pos = smali.getInsnPosByCodeOffset(
                            DbgUtils.classSigToRawFullName(clsSig) + "." + mthSig,
                            getCodeOffset());
                    debuggerPanel.scrollToSmaliLine(cls, Math.max(0, pos), true);
                    return;
                }
            }
            debuggerPanel.log("Can't open smali panel for " + clsSig + "->" + mthSig);
        }
    }

    private void resetCache() {
        this.cache = new StringBuilder(DEFAULT_CACHE_SIZE);
    }

    @Override
    public String toString() {
        StringBuilder sbCache = cache;
        if (sbCache.length() == 0) {
            long off = getCodeOffset();
            if (off < 0) {
                sbCache.append(String.format("index: %-4d ", off));
            } else {
                sbCache.append(String.format("index: %04x ", off));
            }
            if (clsSig == null) {
                sbCache.append("clsID: ").append(frame.getClassID());
            } else {
                sbCache.append(clsSig).append("->");
            }
            if (mthSig == null) {
                sbCache.append(" mthID: ").append(frame.getMethodID());
            } else {
                sbCache.append(mthSig);
            }
        }
        return sbCache.toString();
    }
}
