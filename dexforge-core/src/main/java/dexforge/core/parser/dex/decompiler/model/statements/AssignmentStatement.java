package dexforge.core.parser.dex.decompiler.model.statements;

import dexforge.core.parser.dex.decompiler.model.expressions.JavaExpression;
import dexforge.core.parser.dex.decompiler.model.expressions.VariableExpression;

public final class AssignmentStatement implements JavaStatement {
    private final VariableExpression variable;
    private final JavaExpression expression;
    private final String type;
    private final boolean isDeclaration;

    public AssignmentStatement(VariableExpression variable, JavaExpression expression, String type, boolean isDeclaration) {
        this.variable = variable;
        this.expression = expression;
        this.type = type;
        this.isDeclaration = isDeclaration;
    }

    @Override
    public String toCode(int indent) {
        String space = "    ".repeat(indent);
        StringBuilder sb = new StringBuilder(space);
        if (isDeclaration && type != null) {
            sb.append(type).append(" ");
        }
        sb.append(variable.toCode()).append(" = ").append(expression.toCode()).append(";");
        return sb.toString();
    }
}
