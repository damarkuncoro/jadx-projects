package dexforge.core.parser.dex.decompiler.model;

import dexforge.core.parser.dex.decompiler.model.statements.JavaStatement;
import java.util.ArrayList;
import java.util.List;

public final class JavaMethod {
    private final String name;
    private final String returnType;
    private final List<String> parameters = new ArrayList<>();
    private final List<JavaStatement> statements = new ArrayList<>();
    private final List<String> annotations = new ArrayList<>();

    public JavaMethod(String name, String returnType) {
        this.name = name;
        this.returnType = returnType;
    }

    public String getName() { return name; }
    public String getReturnType() { return returnType; }
    public List<String> getParameters() { return parameters; }
    public List<JavaStatement> getStatements() { return statements; }
    public List<String> getAnnotations() { return annotations; }

    public String toCode() {
        StringBuilder sb = new StringBuilder();
        for (String ann : annotations) {
            sb.append("    ").append(ann).append("\n");
        }
        sb.append("    public ").append(returnType).append(" ").append(name).append("(");
        sb.append(String.join(", ", parameters)).append(") {\n");
        for (JavaStatement stmt : statements) {
            sb.append(stmt.toCode(2)).append("\n");
        }
        sb.append("    }\n");
        return sb.toString();
    }
}
