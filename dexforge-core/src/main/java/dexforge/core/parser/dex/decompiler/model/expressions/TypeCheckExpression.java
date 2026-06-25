package dexforge.core.parser.dex.decompiler.model.expressions;

public final class TypeCheckExpression implements JavaExpression {
    private final JavaExpression object;
    private final String targetType;
    private final boolean isCast;

    public TypeCheckExpression(JavaExpression object, String targetType, boolean isCast) {
        this.object = object;
        this.targetType = targetType;
        this.isCast = isCast;
    }

    @Override
    public String toCode() {
        String javaType = targetType;
        if (targetType.startsWith("L") && targetType.endsWith(";")) {
            javaType = targetType.substring(1, targetType.length() - 1).replace('/', '.');
        }

        if (isCast) {
            return "((" + javaType + ") " + object.toCode() + ")";
        } else {
            return object.toCode() + " instanceof " + javaType;
        }
    }
}
