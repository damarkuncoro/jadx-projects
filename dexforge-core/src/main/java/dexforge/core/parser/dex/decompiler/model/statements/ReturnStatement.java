package dexforge.core.parser.dex.decompiler.model.statements;

public final class ReturnStatement implements JavaStatement {
    private final String expression;

    public ReturnStatement(String expression) {
        this.expression = expression;
    }

    @Override
    public String toCode(int indent) {
        String space = "    ".repeat(indent);
        if (expression == null || expression.isEmpty() || expression.equals("void")) {
            return space + "return;";
        }
        return space + "return " + expression + ";";
    }
}
