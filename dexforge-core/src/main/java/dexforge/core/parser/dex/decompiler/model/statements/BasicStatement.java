package dexforge.core.parser.dex.decompiler.model.statements;

public final class BasicStatement implements JavaStatement {
    private final String content;

    public BasicStatement(String content) {
        this.content = content;
    }

    @Override
    public String toCode(int indent) {
        return "    ".repeat(indent) + content + (content.endsWith(";") || content.startsWith("//") ? "" : ";");
    }
}
