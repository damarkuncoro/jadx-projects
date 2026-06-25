package dexforge.core.parser.dex.decompiler.model.expressions;

public final class UnaryExpression implements JavaExpression {
    private final String operator;
    private final JavaExpression expression;
    private final boolean postfix;

    public UnaryExpression(String operator, JavaExpression expression, boolean postfix) {
        this.operator = operator;
        this.expression = expression;
        this.postfix = postfix;
    }

    @Override
    public String toCode() {
        if (postfix) {
            return expression.toCode() + operator;
        }
        return operator + expression.toCode();
    }
}
