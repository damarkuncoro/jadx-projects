package jadx.gui.device.debugger;

import jadx.core.utils.StringUtils;
import jadx.gui.device.debugger.smali.SmaliRegister;
import org.jetbrains.annotations.Nullable;

public class RegTreeNode extends RuntimeValueTreeNode {
    private static final long serialVersionUID = -1111111202103122234L;

    private final SmaliRegister smaliReg;
    private SmaliDebugger.RuntimeRegister runtimeReg;
    private String value;
    private String type;
    private String alias;
    private boolean absType;

    public RegTreeNode(SmaliRegister smaliReg) {
        this.smaliReg = smaliReg;
    }

    public void updateReg(SmaliDebugger.RuntimeRegister reg) {
        runtimeReg = reg;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    @Override
    public RegTreeNode updateValue(String value) {
        setUpdated(true);
        this.value = value;
        removeAllChildren();
        return this;
    }

    @Override
    public RegTreeNode updateType(String type) {
        if (this.type == null || !this.type.equals(type)) {
            this.type = type;
            reset();
        }
        return this;
    }

    private void reset() {
        value = null;
        removeAllChildren();
        setUpdated(true);
        this.absType = false;
        updateTypeID(0);
    }

    @Override
    public String getName() {
        if (!StringUtils.isEmpty(alias)) {
            return String.format("%s (%s)", smaliReg.getName(), alias);
        }
        return String.format("%-3s", smaliReg.getName());
    }

    @Override
    @Nullable
    public String getValue() {
        return value;
    }

    public SmaliDebugger.RuntimeRegister getRuntimeReg() {
        return runtimeReg;
    }

    public int getRuntimeRegNum() {
        return smaliReg.getRuntimeRegNum();
    }

    @Override
    public String getType() {
        if (type != null) {
            return type;
        }
        if (runtimeReg != null) {
            return runtimeReg.getType().getDesc();
        }
        return null;
    }

    @Override
    public SmaliDebugger.RuntimeValue getRuntimeValue() {
        return getRuntimeReg();
    }

    @Override
    public boolean isAbsoluteType() {
        return absType;
    }

    public void setAbsoluteType(boolean abs) {
        absType = abs;
    }
}
