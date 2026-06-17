package jadx.gui.device.debugger;

import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.nodes.ClassNode;
import jadx.core.dex.nodes.FieldNode;
import jadx.core.utils.StringUtils;
import jadx.gui.device.debugger.smali.Smali;
import jadx.gui.device.debugger.smali.SmaliRegister;
import jadx.gui.treemodel.JClass;
import jadx.gui.ui.panel.JDebuggerPanel;
import jadx.gui.utils.UiUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DebuggerDataManager {
    private static final Logger LOG = LoggerFactory.getLogger(DebuggerDataManager.class);
    private static final Map<String, RuntimeType> TYPE_MAP = new HashMap<>();
    private static final RuntimeType[] POSSIBLE_TYPES = {RuntimeType.OBJECT, RuntimeType.INT, RuntimeType.LONG};
    private static final int DEFAULT_CACHE_SIZE = 512;

    private final Map<String, RegisterObserver> regAdaMap = new ConcurrentHashMap<>();
    private final ExecutorService lazyQueue = Executors.newSingleThreadExecutor();
    private final JDebuggerPanel debuggerPanel;
    private final SmaliDebugger debugger;
    private final ArtAdapter.IArtAdapter art;
    private final DebuggerState state = new DebuggerState();
    private boolean updateAllFldAndReg = false;
    private RuntimeValueTreeNode toBeUpdatedTreeNode = null;

    public DebuggerDataManager(JDebuggerPanel debuggerPanel, SmaliDebugger debugger, ArtAdapter.IArtAdapter art) {
        this.debuggerPanel = debuggerPanel;
        this.debugger = debugger;
        this.art = art;
        initTypeMap();
    }

    private static void initTypeMap() {
        if (TYPE_MAP.isEmpty()) {
            TYPE_MAP.put("I", RuntimeType.INT);
            TYPE_MAP.put("Z", RuntimeType.INT);
            TYPE_MAP.put("B", RuntimeType.INT);
            TYPE_MAP.put("C", RuntimeType.INT);
            TYPE_MAP.put("F", RuntimeType.INT);
            TYPE_MAP.put("S", RuntimeType.INT);
            TYPE_MAP.put("V", RuntimeType.INT);
            TYPE_MAP.put("int", RuntimeType.INT);
            TYPE_MAP.put("boolean", RuntimeType.INT);
            TYPE_MAP.put("byte", RuntimeType.INT);
            TYPE_MAP.put("short", RuntimeType.INT);
            TYPE_MAP.put("char", RuntimeType.INT);
            TYPE_MAP.put("float", RuntimeType.INT);
            TYPE_MAP.put("void", RuntimeType.INT);
            TYPE_MAP.put("L", RuntimeType.LONG);
            TYPE_MAP.put("D", RuntimeType.LONG);
            TYPE_MAP.put("long", RuntimeType.LONG);
            TYPE_MAP.put("double", RuntimeType.LONG);
            TYPE_MAP.put("java.lang.String", RuntimeType.STRING);
            TYPE_MAP.put("Ljava/lang/String;", RuntimeType.STRING);
        }
    }

    public boolean updateRegister(RegTreeNode regNode, RuntimeType type, boolean retry) {
        if (type == null) {
            if (regNode.isAbsoluteType()) {
                type = castType(regNode.getType());
            } else {
                type = POSSIBLE_TYPES[0];
            }
        }
        boolean ok = false;
        SmaliDebugger.RuntimeRegister register = null;
        try {
            register = debugger.getRegisterSync(
                    state.frame.getThreadID(),
                    state.frame.getFrame().getID(),
                    regNode.getRuntimeRegNum(),
                    type);
        } catch (SmaliDebuggerException e) {
            if (retry) {
                if (debugger.errIsTypeMismatched(e.getErrCode())) {
                    RuntimeType[] types = getPossibleTypes(type);
                    for (RuntimeType nextType : types) {
                        ok = updateRegister(regNode, nextType, false);
                        if (ok) {
                            regNode.updateType(nextType.getDesc());
                            break;
                        }
                    }
                } else {
                    logErr(e.getMessage() + " for " + regNode.getName());
                    regNode.updateType(null);
                    regNode.updateValue(null);
                }
            }
        }
        if (register != null) {
            regNode.updateReg(register);
            decodeRuntimeValue(regNode);
        }
        debuggerPanel.updateRegTree(regNode);
        return ok;
    }

    public void updateField(FieldTreeNode node) {
        try {
            setFieldsNotUpdated();
            debugger.getValueSync(node.getObjectID(), node.getRuntimeField());
            decodeRuntimeValue(node);
            debuggerPanel.updateThisTree(node);
        } catch (SmaliDebuggerException e) {
            logErr(e);
        }
    }

    private RuntimeType[] getPossibleTypes(RuntimeType cur) {
        RuntimeType[] types = new RuntimeType[2];
        for (int i = 0, j = 0; i < POSSIBLE_TYPES.length; i++) {
            if (cur != POSSIBLE_TYPES[i]) {
                types[j++] = POSSIBLE_TYPES[i];
            }
        }
        return types;
    }

    private RuntimeType castType(String type) {
        RuntimeType rt = null;
        if (!StringUtils.isEmpty(type)) {
            rt = TYPE_MAP.get(type);
        }
        if (rt == null) {
            rt = POSSIBLE_TYPES[0];
        }
        return rt;
    }

    private boolean decodeRuntimeValue(RuntimeValueTreeNode valNode) {
        SmaliDebugger.RuntimeValue rValue = valNode.getRuntimeValue();
        RuntimeType type = rValue.getType();
        if (!valNode.isAbsoluteType()) {
            valNode.updateType(null);
        }
        try {
            switch (type) {
                case OBJECT:
                    return decodeObject(valNode);
                case STRING:
                    String str = "\"" + debugger.readStringSync(rValue) + "\"";
                    valNode.updateType("java.lang.String")
                            .updateTypeID(debugger.readID(rValue))
                            .updateValue(str);
                    break;
                case INT:
                    valNode.updateValue(Integer.toString(debugger.readInt(rValue)));
                    break;
                case LONG:
                    valNode.updateValue(Long.toString(debugger.readAll(rValue)));
                    break;
                case ARRAY:
                    decodeArrayVal(valNode);
                    break;
                case BOOLEAN: {
                    int b = debugger.readByte(rValue);
                    valNode.updateValue(b == 1 ? "true" : "false");
                    break;
                }
                case SHORT:
                    valNode.updateValue(Short.toString(debugger.readShort(rValue)));
                    break;
                case CHAR:
                case BYTE: {
                    int b = (int) debugger.readAll(rValue);
                    if (DbgUtils.isPrintableChar(b)) {
                        valNode.updateValue(type == RuntimeType.CHAR ? String.valueOf((char) b) : String.valueOf((byte) b));
                    } else {
                        valNode.updateValue(String.valueOf(b));
                    }
                    break;
                }
                case DOUBLE:
                    double d = debugger.readDouble(rValue);
                    valNode.updateValue(Double.toString(d));
                    break;
                case FLOAT:
                    float f = debugger.readFloat(rValue);
                    valNode.updateValue(Float.toString(f));
                    break;
                case VOID:
                    valNode.updateType("void");
                    break;
                case THREAD:
                    valNode.updateType("thread").updateTypeID(debugger.readID(rValue));
                    break;
                case THREAD_GROUP:
                    valNode.updateType("thread_group").updateTypeID(debugger.readID(rValue));
                    break;
                case CLASS_LOADER:
                    valNode.updateType("class_loader").updateTypeID(debugger.readID(rValue));
                    break;
                case CLASS_OBJECT:
                    valNode.updateType("class_object").updateTypeID(debugger.readID(rValue));
                    break;
            }
        } catch (SmaliDebuggerException e) {
            logErr(e);
            return false;
        }
        return true;
    }

    private boolean decodeObject(RuntimeValueTreeNode valNode) {
        SmaliDebugger.RuntimeValue rValue = valNode.getRuntimeValue();
        boolean ok = true;
        if (debugger.readID(rValue) == 0) {
            if (valNode.isAbsoluteType()) {
                valNode.updateValue("null");
                return ok;
            } else if (!art.readNullObject()) {
                valNode.updateType(art.typeForNull());
                valNode.updateValue("0");
                return ok;
            }
        }
        String sig;
        try {
            sig = debugger.readObjectSignatureSync(rValue);
            valNode.updateType(String.format("%s@%d", DbgUtils.classSigToRawFullName(sig),
                    debugger.readID(rValue)));
        } catch (SmaliDebuggerException e) {
            ok = debugger.errIsInvalidObject(e.getErrCode()) && valNode instanceof RegTreeNode;
            if (ok) {
                try {
                    RegTreeNode reg = (RegTreeNode) valNode;
                    SmaliDebugger.RuntimeRegister rr = debugger.getRegisterSync(
                            state.frame.getThreadID(),
                            state.frame.getFrame().getID(),
                            reg.getRuntimeRegNum(), RuntimeType.INT);
                    reg.updateReg(rr);
                    rValue = rr;
                    valNode.updateType(RuntimeType.INT.getDesc());
                    valNode.updateValue(Long.toString((int) debugger.readAll(rValue)));
                } catch (SmaliDebuggerException except) {
                    logErr(except, String.format("Update %s failed, %s", valNode.getName(), except.getMessage()));
                    valNode.updateValue(except.getMessage());
                    ok = false;
                }
            } else {
                logErr(e);
            }
        }
        return ok;
    }

    private void decodeArrayVal(RuntimeValueTreeNode valNode) throws SmaliDebuggerException {
        String type = debugger.readObjectSignatureSync(valNode.getRuntimeValue());
        ArgType argType = ArgType.parse(type);
        String javaType = argType.toString();
        Map.Entry<Integer, List<Long>> ret = debugger.readArray(valNode.getRuntimeValue(), 0, 0);
        javaType = javaType.substring(0, javaType.length() - 1) + ret.getKey() + "]";
        valNode.updateType(javaType + "@" + debugger.readID(valNode.getRuntimeValue()));

        if (argType.getArrayElement().isPrimitive()) {
            for (Long aLong : ret.getValue()) {
                valNode.add(new DefaultMutableTreeNode(Long.toString(aLong)));
            }
            return;
        }
        String typeSig = type.substring(1);
        if (DbgUtils.isStringObjectSig(typeSig)) {
            for (Long aLong : ret.getValue()) {
                valNode.add(new DefaultMutableTreeNode(debugger.readStringSync(aLong)));
            }
            return;
        }
        typeSig = DbgUtils.classSigToRawFullName(typeSig);
        for (Long aLong : ret.getValue()) {
            valNode.add(new DefaultMutableTreeNode(String.format("%s@%d", typeSig, aLong)));
        }
    }

    public void markNextToBeUpdated(long codeOffset) {
        if (codeOffset > -1) {
            Object rst = state.smali.getResultRegOrField(state.mthFullID, codeOffset);
            toBeUpdatedTreeNode = null;
            if (state.frame != null) {
                if (rst instanceof Integer) {
                    int regNum = (int) rst;
                    if (state.frame.getRegNodes().size() > regNum) {
                        toBeUpdatedTreeNode = state.frame.getRegNodes().get(regNum);
                    }
                    return;
                }
                if (rst instanceof FieldNode) {
                    FieldNode info = (FieldNode) rst;
                    toBeUpdatedTreeNode = state.frame.getFieldNodes()
                            .stream()
                            .filter(f -> f.getName().equals(info.getName()))
                            .findFirst()
                            .orElse(null);
                }
            }
        }
    }

    public FrameNode updateAllStackFrames(long threadID) {
        List<SmaliDebugger.Frame> frames = Collections.emptyList();
        try {
            frames = debugger.getFramesSync(threadID);
        } catch (SmaliDebuggerException e) {
            logErr(e);
        }
        if (frames.isEmpty()) {
            return null;
        }
        List<FrameNode> frameEleList = new ArrayList<>(frames.size());
        for (SmaliDebugger.Frame frame : frames) {
            FrameNode ele = new FrameNode(debuggerPanel, threadID, frame);
            frameEleList.add(ele);
        }
        FrameNode curEle = frameEleList.get(0);
        fetchStackFrameNames(curEle);

        debuggerPanel.refreshStackFrameList(frameEleList);
        lazyQueue.execute(() -> {
            for (int i = 1; i < frameEleList.size(); i++) {
                fetchStackFrameNames(frameEleList.get(i));
            }
            debuggerPanel.refreshStackFrameList(Collections.emptyList());
        });
        return frameEleList.get(0);
    }

    public void fetchStackFrameNames(FrameNode ele) {
        try {
            long clsId = ele.getFrame().getClassID();
            String clsSig = debugger.getClassSignatureSync(clsId);
            String mthSig = debugger.getMethodSignatureSync(clsId, ele.getFrame().getMethodID());
            ele.setSignatures(clsSig, mthSig);
        } catch (SmaliDebuggerException e) {
            logErr(e);
        }
    }

    public Smali decodeSmali(FrameNode frame) {
        if (state.frame.getClsSig() != null) {
            JClass cls = DbgUtils.getTopClassBySig(frame.getClsSig(), debuggerPanel.getMainWindow());
            if (cls != null) {
                ClassNode cNode = cls.getCls().getClassNode();
                state.clsNode = cls;
                state.mthFullID = DbgUtils.classSigToRawFullName(frame.getClsSig()) + "." + frame.getMthSig();
                return DbgUtils.getSmali(cNode);
            }
        }
        return null;
    }

    public void updateAllFields(FrameNode frame) {
        List<FieldNode> fldNodes = Collections.emptyList();
        String clsSig = frame.getClsSig();
        if (clsSig != null) {
            ClassNode clsNode = DbgUtils.getClassNodeBySig(clsSig, debuggerPanel.getMainWindow());
            if (clsNode != null) {
                fldNodes = clsNode.getFields();
            }
        }
        try {
            long thisId = debugger.getThisID(frame.getThreadID(), frame.getFrame().getID());
            List<SmaliDebugger.RuntimeField> flds = debugger.getAllFieldsSync(frame.getClsID());
            List<FieldTreeNode> nodes = new ArrayList<>(flds.size());
            for (SmaliDebugger.RuntimeField fld : flds) {
                FieldTreeNode fldNode = new FieldTreeNode(fld, thisId);
                fldNodes.stream()
                        .filter(f -> f.getName().equals(fldNode.getName()))
                        .findFirst()
                        .ifPresent(smaliFld -> fldNode.setAlias(smaliFld.getAlias()));
                nodes.add(fldNode);
            }
            debuggerPanel.updateThisFieldNodes(nodes);
            frame.setFieldNodes(nodes);
            if (thisId > 0 && nodes.size() > 0) {
                lazyQueue.execute(() -> updateAllFieldValues(thisId, frame));
            }
        } catch (SmaliDebuggerException e) {
            logErr(e);
        }
    }

    private void updateAllFieldValues(long thisId, FrameNode frame) {
        List<FieldTreeNode> nodes = frame.getFieldNodes();
        if (nodes.size() > 0) {
            List<FieldTreeNode> flds = new ArrayList<>(nodes.size());
            List<SmaliDebugger.RuntimeField> rts = new ArrayList<>(nodes.size());
            nodes.forEach(n -> {
                SmaliDebugger.RuntimeField f = n.getRuntimeField();
                if (f.isBelongToThis()) {
                    flds.add(n);
                    rts.add(f);
                }
            });
            try {
                debugger.getAllFieldValuesSync(thisId, rts);
                flds.forEach(this::decodeRuntimeValue);
                debuggerPanel.refreshThisFieldTree();
            } catch (SmaliDebuggerException e) {
                logErr(e);
            }
        }
    }

    public void updateAllRegisters(FrameNode frame) {
        UiUtils.uiRun(() -> {
            if (!buildRegTreeNodes(frame).isEmpty()) {
                fetchAllRegisters(frame);
            }
        });
    }

    private List<RegTreeNode> buildRegTreeNodes(FrameNode frame) {
        List<SmaliRegister> regs = state.smali.getRegisterList(state.mthFullID);
        List<RegTreeNode> regNodes = new ArrayList<>(regs.size());
        List<RegTreeNode> inRtOrder = new ArrayList<>(regs.size());

        regs.forEach(r -> {
            RegTreeNode rn = new RegTreeNode(r);
            regNodes.add(rn);
            inRtOrder.add(rn);
        });
        inRtOrder.sort(Comparator.comparingInt(RegTreeNode::getRuntimeRegNum));
        frame.setRegNodes(regNodes);
        debuggerPanel.updateRegTreeNodes(inRtOrder);
        debuggerPanel.refreshRegisterTree();
        return regNodes;
    }

    private void fetchAllRegisters(FrameNode frame) {
        List<SmaliRegister> regs = state.regAdapter.getInitializedList(frame.getCodeOffset());
        for (SmaliRegister reg : regs) {
            SmaliDebugger.RuntimeVarInfo info = state.regAdapter.getInfo(reg.getRuntimeRegNum(), frame.getCodeOffset());
            RegTreeNode regNode = frame.getRegNodes().get(reg.getRegNum());
            if (info != null) {
                applyDbgInfo(regNode, info);
            }
            updateRegister(regNode, null, true);
        }
    }

    private void applyDbgInfo(RegTreeNode rn, SmaliDebugger.RuntimeVarInfo info) {
        applyDbgInfo(rn, info.getName(), info.getType());
    }

    private void applyDbgInfo(RegTreeNode rn, String alias, String type) {
        rn.setAlias(alias);
        rn.updateType(type);
        rn.setAbsoluteType(true);
    }

    private void setRegsNotUpdated() {
        if (state.frame != null) {
            for (RegTreeNode regNode : state.frame.getRegNodes()) {
                regNode.setUpdated(false);
            }
        }
    }

    private void setFieldsNotUpdated() {
        if (state.frame != null) {
            for (FieldTreeNode node : state.frame.getFieldNodes()) {
                node.setUpdated(false);
            }
        }
    }

    public List<SmaliRegister> getSmaliRegisterList() {
        int regCount = state.smali.getRegCount(state.mthFullID);
        int paramStart = state.smali.getParamRegStart(state.mthFullID);
        List<SmaliRegister> srs = state.smali.getRegisterList(state.mthFullID);
        for (SmaliRegister sr : srs) {
            sr.setRuntimeRegNum(art.getRuntimeRegNum(sr.getRegNum(), regCount, paramStart));
        }
        return srs;
    }

    public void resetAllInfo() {
        toBeUpdatedTreeNode = null;
        state.reset();
        UiUtils.uiRun(debuggerPanel::resetAllDebuggingInfo);
    }

    public List<SmaliDebugger.RuntimeVarInfo> getRuntimeDebugInfo(FrameNode frame) {
        try {
            SmaliDebugger.RuntimeDebugInfo dbgInfo = debugger.getRuntimeDebugInfo(frame.getClsID(), frame.getMthID());
            if (dbgInfo != null) {
                return dbgInfo.getInfoList();
            }
        } catch (SmaliDebuggerException ignored) {
        }
        return Collections.emptyList();
    }

    public void scrollToPos(long codeOffset) {
        int pos = -1;
        if (codeOffset > -1) {
            pos = state.smali.getInsnPosByCodeOffset(state.mthFullID, codeOffset);
        }
        if (pos == -1) {
            pos = state.smali.getMethodDefPos(state.mthFullID);
            if (pos == -1) {
                debuggerPanel.log("Can't scroll to " + state.mthFullID);
                return;
            }
        }
        debuggerPanel.scrollToSmaliLine(state.clsNode, pos, true);
    }

    public void refreshRegInfo(long codeOffset) {
        List<RegisterObserver.Info> list = state.regAdapter.getInfoAt(codeOffset);
        for (RegisterObserver.Info info : list) {
            RegTreeNode reg = state.frame.getRegNodes().get(info.getSmaliRegNum());
            if (info.isLoad()) {
                applyDbgInfo(reg, info.getName(), info.getType());
            } else {
                reg.setAlias("");
                reg.setAbsoluteType(false);
            }
        }
        if (list.size() > 0) {
            debuggerPanel.refreshRegisterTree();
        }
    }

    public Map<String, RegisterObserver> getRegAdaMap() {
        return regAdaMap;
    }

    public DebuggerState getState() {
        return state;
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

    public void setUpdateAllFldAndReg(boolean updateAllFldAndReg) {
        this.updateAllFldAndReg = updateAllFldAndReg;
    }

    public boolean isUpdateAllFldAndReg() {
        return updateAllFldAndReg;
    }

    public void setToBeUpdatedTreeNode(RuntimeValueTreeNode toBeUpdatedTreeNode) {
        this.toBeUpdatedTreeNode = toBeUpdatedTreeNode;
    }

    public RuntimeValueTreeNode getToBeUpdatedTreeNode() {
        return toBeUpdatedTreeNode;
    }

    public void refreshCurFrame(long threadID, long codeOffset) {
        try {
            SmaliDebugger.Frame frame = debugger.getCurrentFrame(threadID);
            state.frame.setFrame(frame);
            state.frame.updateCodeOffset(codeOffset);
        } catch (SmaliDebuggerException e) {
            logErr(e);
        }
    }

    public void updateRegOrField(RuntimeValueTreeNode valTreeNode) {
        if (valTreeNode instanceof RegTreeNode) {
            updateRegister((RegTreeNode) valTreeNode, null, true);
            return;
        }
        if (valTreeNode instanceof FieldTreeNode) {
            updateField((FieldTreeNode) valTreeNode);
            return;
        }
    }
}
