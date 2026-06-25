package dexforge.core.parser.dex.decompiler.model.statements;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public final class SwitchStatement implements JavaStatement {
    private final String expression;
    private final TreeMap<Integer, List<JavaStatement>> cases = new TreeMap<>();
    private final List<JavaStatement> defaultCase = new ArrayList<>();

    public SwitchStatement(String expression) {
        this.expression = expression;
    }

    public void addCase(int value, List<JavaStatement> statements) {
        cases.put(value, statements);
    }

    public List<JavaStatement> getDefaultCase() { return defaultCase; }

    @Override
    public String toCode(int indent) {
        String space = "    ".repeat(indent);
        StringBuilder sb = new StringBuilder();
        sb.append(space).append("switch (").append(expression).append(") {\n");

        for (var entry : cases.entrySet()) {
            sb.append(space).append("    case ").append(entry.getKey()).append(":\n");
            for (JavaStatement stmt : entry.getValue()) {
                sb.append(stmt.toCode(indent + 2)).append("\n");
            }
            sb.append(space).append("        break;\n");
        }

        if (!defaultCase.isEmpty()) {
            sb.append(space).append("    default:\n");
            for (JavaStatement stmt : defaultCase) {
                sb.append(stmt.toCode(indent + 2)).append("\n");
            }
        }

        sb.append(space).append("}");
        return sb.toString();
    }
}
