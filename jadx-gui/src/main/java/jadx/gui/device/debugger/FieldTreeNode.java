package jadx.gui.device.debugger;

import jadx.core.dex.instructions.args.ArgType;
import jadx.core.utils.StringUtils;

public class FieldTreeNode extends RuntimeValueTreeNode {
    private static final long serialVersionUID = -1111111202103122235L;

    private final SmaliDebugger.RuntimeField field;
    private String value;
    private String alias;
    private long objectID;

    FieldTreeNode(SmaliDebugger.RuntimeField field, long id) {
        this.field = field;
        objectID = id;
    }

    public long getObjectID() {
        return objectID;
    }

    public void setObjectID(long id) {
        this.objectID = id;
    }

    public SmaliDebugger.RuntimeField getRuntimeField() {
        return this.field;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    @Override
    public FieldTreeNode updateValue(String val) {
        setUpdated(true);
        value = val;
        removeAllChildren();
        return this;
    }

    @Override
    public FieldTreeNode updateType(String val) {
        return this;
    }

    @Override
    public String getName() {
        if (StringUtils.isEmpty(alias) || alias.equals(field.getName())) {
            return field.getName();
        }
        return field.getName() + " (" + alias + ")";
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String getType() {
        return ArgType.parse(field.getFieldType()).toString();
    }

    @Override
    public SmaliDebugger.RuntimeValue getRuntimeValue() {
        return field;
    }

    @Override
    public boolean isAbsoluteType() {
        return true;
    }
}
