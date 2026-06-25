package dexforge.core.parser.dex.decompiler.model.expressions;

public final class NewArrayExpression implements JavaExpression {
    private final String type;
    private final JavaExpression size;

    public NewArrayExpression(String type, JavaExpression size) {
        this.type = type;
        this.size = size;
    }

    @Override
    public String toCode() {
        String javaType = type;
        if (type.startsWith("[L") && type.endsWith(";")) {
            javaType = type.substring(2, type.length() - 1).replace('/', '.');
        } else if (type.startsWith("[")) {
            // Handle primitive arrays like [I -> int
            switch (type.charAt(1)) {
                case 'I': javaType = "int"; break;
                case 'Z': javaType = "boolean"; break;
                case 'B': javaType = "byte"; break;
                case 'C': javaType = "char"; break;
                case 'S': javaType = "short"; break;
                case 'J': javaType = "long"; break;
                case 'F': javaType = "float"; break;
                case 'D': javaType = "double"; break;
            }
        }
        return "new " + javaType + "[" + size.toCode() + "]";
    }
}
