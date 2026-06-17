package jadx.gui.device.debugger;

import jadx.core.dex.instructions.args.ArgType;
import jadx.core.utils.exceptions.JadxRuntimeException;
import jadx.gui.device.debugger.smali.Smali;
import jadx.gui.treemodel.JClass;
import jadx.gui.ui.panel.IDebugController;
import jadx.gui.ui.panel.JDebuggerPanel;
import jadx.gui.utils.UiUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class DebugController implements SmaliDebugger.SuspendListener, IDebugController {
    private static final Logger LOG = LoggerFactory.getLogger(DebugController.class);
    private static final String ONCREATE_SIGNATURE = "onCreate(Landroid/os/Bundle;)V";
    private static final Map<String, RuntimeType> TYPE_MAP = new HashMap<>();
    private static final RuntimeType[] POSSIBLE_TYPES = {RuntimeType.OBJECT, RuntimeType.INT, RuntimeType.LONG};

    private JDebuggerPanel debuggerPanel;
    private SmaliDebugger debugger;
    private ArtAdapter.IArtAdapter art;
    private volatile boolean isSuspended = true;
    private boolean hasResumed;
    private ResumeCmd run;
    private ResumeCmd stepOver;
    private ResumeCmd stepInto;
    private ResumeCmd stepOut;
    private StateListener stateListener;
    private final ExecutorService updateQueue = Executors.newSingleThreadExecutor();
    private final ExecutorService lazyQueue = Executors.newSingleThreadExecutor();
    private DebuggerDataManager dataManager;
    private DebuggerBreakpointManager bpManager;

    @Override
    public boolean startDebugger(JDebuggerPanel debuggerPanel, String adbHost, int adbPort, int androidVer) {
        this.debuggerPanel = debuggerPanel;
        UiUtils.uiRunAndWait(debuggerPanel::resetUI);
        try {
            debugger = SmaliDebugger.attach(adbHost, adbPort, this);
        } catch (SmaliDebuggerException e) {
            JOptionPane.showMessageDialog(debuggerPanel.getMainWindow(), e.getMessage(),
                    jadx.gui.utils.NLS.str("error_dialog.title"), JOptionPane.ERROR_MESSAGE);
            logErr(e);
            return false;
        }
        art = ArtAdapter.getAdapter(androidVer);
        this.dataManager = new DebuggerDataManager(debuggerPanel, debugger, art);
        this.bpManager = new DebuggerBreakpointManager(debugger, debuggerPanel);
        resetAllInfo();
        hasResumed = false;
        run = debugger::resume;
        stepOver = debugger::stepOver;
        stepInto = debugger::stepInto;
        stepOut = debugger::stepOut;
        stopAtOnCreate();
        BreakpointManager.setDebugController(this);
        initBreakpoints(BreakpointManager.getAllBreakpoints());
        return true;
    }

    private void openMainActivityTab(JClass mainActivity) {
        String fullID = DbgUtils.getRawFullName(mainActivity) + "." + ONCREATE_SIGNATURE;
        Smali smali = DbgUtils.getSmali(mainActivity.getCls().getClassNode());
        int pos = smali.getMethodDefPos(fullID);
        int finalPos = Math.max(1, pos);
        debuggerPanel.scrollToSmaliLine(mainActivity, finalPos, true);
    }

    private void stopAtOnCreate() {
        DbgUtils.AppData appData = DbgUtils.parseAppData(debuggerPanel.getMainWindow());
        if (appData == null) {
            debuggerPanel.log("Failed to set breakpoint at onCreate, you have to do it yourself.");
            return;
        }
        JClass mainActivity = DbgUtils.getJClass(appData.getMainActivityCls(), debuggerPanel.getMainWindow());
        lazyQueue.execute(() -> openMainActivityTab(mainActivity));
        String clsSig = DbgUtils.getRawFullName(mainActivity);
        try {
            long id = debugger.getClassID(clsSig, true);
            if (id != -1) {
                return;
            }
            debuggerPanel.log(String.format("Breakpoint will set at %s.%s", clsSig, ONCREATE_SIGNATURE));
            debugger.regMethodEntryEventSync(clsSig, ONCREATE_SIGNATURE::equals);
        } catch (SmaliDebuggerException e) {
            logErr(e, String.format("Failed set breakpoint at %s.%s", clsSig, ONCREATE_SIGNATURE));
        }
    }

    @Override
    public boolean isSuspended() {
        return isSuspended;
    }

    @Override
    public boolean isDebugging() {
        return debugger != null;
    }

    @Override
    public boolean run() {
        return execResumeCmd(run);
    }

    @Override
    public boolean stepInto() {
        return execResumeCmd(stepInto);
    }

    @Override
    public boolean stepOver() {
        return execResumeCmd(stepOver);
    }

    @Override
    public boolean stepOut() {
        return execResumeCmd(stepOut);
    }

    @Override
    public boolean pause() {
        if (isDebugging()) {
            try {
                debugger.suspend();
            } catch (SmaliDebuggerException e) {
                logErr(e);
                return false;
            }
            setDebuggerState(true, false);
            resetAllInfo();
        }
        return true;
    }

    @Override
    public boolean stop() {
        if (isDebugging()) {
            try {
                debugger.exit();
            } catch (SmaliDebuggerException e) {
                logErr(e);
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean exit() {
        if (isDebugging()) {
            setDebuggerState(true, true);
            stop();
            debugger = null;
        }
        BreakpointManager.setDebugController(null);
        debuggerPanel.getMainWindow().destroyDebuggerPanel();
        debuggerPanel = null;
        return true;
    }

    @Override
    public boolean modifyRegValue(JDebuggerPanel.ValueTreeNode valNode, ArgType type, Object value) {
        checkType(type, value);
        if (isDebugging() && isSuspended()) {
            return modifyValueInternal((RuntimeValueTreeNode) valNode, castType(type), value);
        }
        return false;
    }

    @Override
    public String getProcessName() {
        DbgUtils.AppData appData = DbgUtils.parseAppData(debuggerPanel.getMainWindow());
        if (appData == null) {
            return "";
        }
        return appData.getProcessName();
    }

    private RuntimeType castType(ArgType type) {
        if (type == ArgType.INT) {
            return RuntimeType.INT;
        }
        if (type == ArgType.STRING) {
            return RuntimeType.STRING;
        }
        if (type == ArgType.LONG) {
            return RuntimeType.LONG;
        }
        if (type == ArgType.FLOAT) {
            return RuntimeType.FLOAT;
        }
        if (type == ArgType.DOUBLE) {
            return RuntimeType.DOUBLE;
        }
        if (type == ArgType.OBJECT) {
            return RuntimeType.OBJECT;
        }
        throw new JadxRuntimeException("Unexpected type: " + type);
    }

    private void checkType(ArgType type, Object value) {
        if (!(type == ArgType.INT && value instanceof Integer)
                && !(type == ArgType.STRING && value instanceof String)
                && !(type == ArgType.LONG && value instanceof Long)
                && !(type == ArgType.FLOAT && value instanceof Float)
                && !(type == ArgType.DOUBLE && value instanceof Double)
                && !(type == ArgType.OBJECT && value instanceof Long)) {
            throw new JadxRuntimeException("Type must be one of int, long, float, double, String or Object.");
        }
    }

    private boolean modifyValueInternal(RuntimeValueTreeNode valNode, RuntimeType type, Object value) {
        if (valNode instanceof RegTreeNode) {
            try {
                RegTreeNode regNode = (RegTreeNode) valNode;
                debugger.setValueSync(
                        regNode.getRuntimeRegNum(),
                        type,
                        value,
                        dataManager.getState().frame.getThreadID(),
                        dataManager.getState().frame.getFrame().getID());
                lazyQueue.execute(() -> {
                    setRegsNotUpdated();
                    dataManager.updateRegister(regNode, type, true);
                });
            } catch (SmaliDebuggerException e) {
                logErr(e);
                return false;
            }
        } else if (valNode instanceof FieldTreeNode) {
            FieldTreeNode fldNode = (FieldTreeNode) valNode;
            try {
                debugger.setValueSync(
                        fldNode.getObjectID(),
                        fldNode.getRuntimeField().getFieldID(),
                        fldNode.getRuntimeField().getType(),
                        value);
                lazyQueue.execute(() -> {
                    dataManager.updateField(fldNode);
                });
            } catch (SmaliDebuggerException e) {
                logErr(e);
                return false;
            }
        }
        return true;
    }

    private interface ResumeCmd {
        void exec() throws SmaliDebuggerException;
    }

    private boolean execResumeCmd(ResumeCmd cmd) {
        if (!hasResumed) {
            if (cmd != run) {
                return false;
            }
            hasResumed = true;
        }
        if (isDebugging() && isSuspended()) {
            dataManager.setUpdateAllFldAndReg(cmd == run);
            setDebuggerState(false, false);
            try {
                cmd.exec();
                return true;
            } catch (SmaliDebuggerException e) {
                logErr(e);
                setDebuggerState(true, false);
            }
        }
        return false;
    }

    private void setDebuggerState(boolean suspended, boolean stopped) {
        isSuspended = suspended;
        if (stopped) {
            hasResumed = false;
        }
        if (stateListener != null) {
            stateListener.onStateChanged(suspended, stopped);
        }
    }

    @Override
    public void setStateListener(StateListener listener) {
        stateListener = listener;
    }

    @Override
    public void onSuspendEvent(SuspendInfo info) {
        if (!isDebugging()) {
            return;
        }
        if (info.isTerminated()) {
            debuggerPanel.log("Debugger exited.");
            setDebuggerState(true, true);
            debugger = null;
            return;
        }
        setDebuggerState(true, false);
        long threadID = info.getThreadID();
        int refreshLevel = 2;
        if (dataManager.getState().frame != null) {
            if (threadID == dataManager.getState().frame.getThreadID()
                    && info.getClassID() == dataManager.getState().frame.getClsID()
                    && info.getMethodID() == dataManager.getState().frame.getMthID()) {

                refreshLevel = 1;
            }
            setRegsNotUpdated();
        }
        if (refreshLevel == 2) {
            updateAllInfo(threadID, info.getOffset());
        } else {
            if (dataManager.getState().smali != null && dataManager.getState().frame != null) {
                dataManager.refreshRegInfo(info.getOffset());
                dataManager.refreshCurFrame(threadID, info.getOffset());
                if (dataManager.isUpdateAllFldAndReg()) {
                    debuggerPanel.resetRegTreeNodes();
                    dataManager.updateAllRegisters(dataManager.getState().frame);
                } else if (dataManager.getToBeUpdatedTreeNode() != null) {
                    lazyQueue.execute(() -> dataManager.updateRegOrField(dataManager.getToBeUpdatedTreeNode()));
                }
                markCodeOffset(info.getOffset());
            } else {
                debuggerPanel.resetRegTreeNodes();
            }
            if (dataManager.getState().frame != null) {
                dataManager.getState().frame.updateCodeOffset(info.getOffset());
                debuggerPanel.refreshStackFrameList(Collections.emptyList());
            }
        }
    }

    private void updateAllInfo(long threadID, long codeOffset) {
        updateQueue.execute(() -> {
            resetAllInfo();
            dataManager.getState().frame = dataManager.updateAllStackFrames(threadID);
            if (dataManager.getState().frame != null) {
                lazyQueue.execute(() -> dataManager.updateAllFields(dataManager.getState().frame));
                if (dataManager.getState().frame.getClsSig() == null || dataManager.getState().frame.getMthSig() == null) {
                    dataManager.fetchStackFrameNames(dataManager.getState().frame);
                }
                dataManager.getState().smali = dataManager.decodeSmali(dataManager.getState().frame);
                if (dataManager.getState().smali != null) {
                    dataManager.getState().regAdapter = dataManager.getRegAdaMap().computeIfAbsent(dataManager.getState().mthFullID,
                            k -> RegisterObserver.merge(
                                    dataManager.getRuntimeDebugInfo(dataManager.getState().frame),
                                    dataManager.getSmaliRegisterList(),
                                    art,
                                    dataManager.getState().mthFullID));

                    if (dataManager.getState().smali.getRegCount(dataManager.getState().mthFullID) > 0) {
                        dataManager.updateAllRegisters(dataManager.getState().frame);
                    }
                    markCodeOffset(codeOffset);
                }
            }
            updateAllThreads();
        });
    }

    private void updateAllThreads() {
        List<Long> threads;
        try {
            threads = debugger.getAllThreadsSync();
        } catch (SmaliDebuggerException e) {
            logErr(e);
            return;
        }
        List<ThreadBoxElement> threadEleList = new java.util.ArrayList<>(threads.size());
        for (Long thread : threads) {
            ThreadBoxElement ele = new ThreadBoxElement(thread);
            threadEleList.add(ele);
        }
        debuggerPanel.refreshThreadBox(threadEleList);
        lazyQueue.execute(() -> {
            for (ThreadBoxElement ele : threadEleList) {
                try {
                    ele.setName(debugger.getThreadNameSync(ele.getThreadID()));
                } catch (SmaliDebuggerException e) {
                    logErr(e);
                }
            }
            debuggerPanel.refreshThreadBox(Collections.emptyList());
        });
    }

    private void resetAllInfo() {
        isSuspended = true;
        dataManager.setToBeUpdatedTreeNode(null);
        dataManager.resetAllInfo();
    }

    private void markCodeOffset(long codeOffset) {
        dataManager.scrollToPos(codeOffset);
        dataManager.markNextToBeUpdated(codeOffset);
    }

    private void setRegsNotUpdated() {
        if (dataManager.getState().frame != null) {
            for (RegTreeNode regNode : dataManager.getState().frame.getRegNodes()) {
                regNode.setUpdated(false);
            }
        }
    }

    public boolean setBreakpoint(BreakpointManager.FileBreakpoint bp) {
        return bpManager.setBreakpoint(bp);
    }

    private void initBreakpoints(List<BreakpointManager.FileBreakpoint> fbps) {
        bpManager.initBreakpoints(fbps);
    }

    public boolean removeBreakpoint(BreakpointManager.FileBreakpoint fbp) {
        return bpManager.removeBreakpoint(fbp);
    }

    private void logErr(Exception e, String extra) {
        debuggerPanel.log(e.getMessage());
        debuggerPanel.log(extra);
        LOG.error(extra, e);
    }

    private void logErr(Exception e) {
        debuggerPanel.log(e.getMessage());
        LOG.error("Debug error", e);
    }

    private void logErr(String e) {
        debuggerPanel.log(e);
        LOG.error("Debug error: {}", e);
    }
}
