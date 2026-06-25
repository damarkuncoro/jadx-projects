package dexforge.core.parser.dex.decompiler.model.expressions;

public final class VariableExpression implements JavaExpression {
    private final String name;

    public VariableExpression(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toCode() {
        return name;
    }
}
