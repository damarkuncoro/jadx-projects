package dexforge.core.parser.dex.decompiler.model.statements;

import dexforge.core.parser.dex.decompiler.model.expressions.JavaExpression;
import java.util.ArrayList;
import java.util.List;

public final class WhileStatement implements JavaStatement {
    private final JavaExpression condition;
    private final List<JavaStatement> body = new ArrayList<>();

    public WhileStatement(JavaExpression condition) {
        this.condition = condition;
    }

    public List<JavaStatement> getBody() { return body; }

    @Override
    public String toCode(int indent) {
        String space = "    ".repeat(indent);
        StringBuilder sb = new StringBuilder();
        sb.append(space).append("while (").append(condition != null ? condition.toCode() : "true").append(") {\n");
        for (JavaStatement stmt : body) {
            sb.append(stmt.toCode(indent + 1)).append("\n");
        }
        sb.append(space).append("}");
        return sb.toString();
    }
}
