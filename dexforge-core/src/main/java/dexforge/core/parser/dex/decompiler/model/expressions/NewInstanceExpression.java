package dexforge.core.parser.dex.decompiler.model.expressions;

public final class NewInstanceExpression implements JavaExpression {
    private final String type;

    public NewInstanceExpression(String type) {
        this.type = type;
    }

    @Override
    public String toCode() {
        // Simple heuristic to strip L and ; for Java source
        String javaType = type;
        if (type.startsWith("L") && type.endsWith(";")) {
            javaType = type.substring(1, type.length() - 1).replace('/', '.');
        }
        return "new " + javaType + "()";
    }
}
