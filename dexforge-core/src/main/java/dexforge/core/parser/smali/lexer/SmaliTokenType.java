package dexforge.core.parser.smali.lexer;

public enum SmaliTokenType {
    DIRECTIVE,     // .class, .super, .method, etc.
    INSTRUCTION,   // move, invoke-static, etc.
    REGISTER,      // v0, p1, etc.
    LABEL,         // :label
    STRING_LITERAL,// "hello"
    NUMBER_LITERAL,// 0x1, 10, 0.5f
    IDENTIFIER,    // Class names, method names, field names
    ACCESS_FLAG,   // public, private, static
    SYMBOL,        // (, ), {, }, =, :, ->
    COMMENT,       // # ...
    NEWLINE,
    EOF,
    UNKNOWN
}
