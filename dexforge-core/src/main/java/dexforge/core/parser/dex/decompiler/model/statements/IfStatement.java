package dexforge.core.parser.dex.decompiler.model.statements;

import dexforge.core.parser.dex.decompiler.model.expressions.JavaExpression;
import java.util.ArrayList;
import java.util.List;

public final class IfStatement implements JavaStatement {
    private final JavaExpression condition;
    private final List<JavaStatement> thenBranch = new ArrayList<>();
    private final List<JavaStatement> elseBranch = new ArrayList<>();

    public IfStatement(JavaExpression condition) {
        this.condition = condition;
    }

    public List<JavaStatement> getThenBranch() { return thenBranch; }
    public List<JavaStatement> getElseBranch() { return elseBranch; }

    @Override
    public String toCode(int indent) {
        String space = "    ".repeat(indent);
        StringBuilder sb = new StringBuilder();
        sb.append(space).append("if (").append(condition != null ? condition.toCode() : "true").append(") {\n");
        for (JavaStatement stmt : thenBranch) {
            sb.append(stmt.toCode(indent + 1)).append("\n");
        }
        if (!elseBranch.isEmpty()) {
            sb.append(space).append("} else {\n");
            for (JavaStatement stmt : elseBranch) {
                sb.append(stmt.toCode(indent + 1)).append("\n");
            }
        }
        sb.append(space).append("}");
        return sb.toString();
    }
}
