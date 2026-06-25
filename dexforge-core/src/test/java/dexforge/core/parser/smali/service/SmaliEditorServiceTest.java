package dexforge.core.parser.smali.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import java.util.List;

class SmaliEditorServiceTest {

    @Test
    void testGetOutline() {
        String smaliText = ".class public Lcom/example/MyClass;\n" +
                ".super Ljava/lang/Object;\n" +
                "\n" +
                ".method public run()V\n" +
                "    .registers 2\n" +
                "    nop\n" +
                ".end method\n";

        SmaliEditorService service = new SmaliEditorService();
        List<String> outline = service.getOutline(smaliText);

        assertThat(outline).containsExactly(
                "Class: Lcom/example/MyClass;",
                "  Method: run"
        );
    }

    @Test
    void testGetCompletions() {
        SmaliEditorService service = new SmaliEditorService();
        List<String> completions = service.getCompletions("some smali", 1, 1);
        assertThat(completions).isEmpty();
    }
}
