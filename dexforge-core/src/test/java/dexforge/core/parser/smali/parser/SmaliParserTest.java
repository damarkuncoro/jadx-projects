package dexforge.core.parser.smali.parser;

import static org.assertj.core.api.Assertions.assertThat;

import dexforge.core.parser.smali.model.SmaliClass;
import dexforge.core.parser.smali.model.SmaliMethod;
import org.junit.jupiter.api.Test;

class SmaliParserTest {

    @Test
    void testParseSmaliClass() {
        String smaliText = ".class public Lcom/example/MyClass;\n" +
                ".super Ljava/lang/Object;\n" +
                ".source \"MyClass.java\"\n" +
                ".implements Ljava/lang/Runnable;\n" +
                "\n" +
                ".method public run()V\n" +
                "    .registers 2\n" +
                "    nop\n" +
                ".end method\n";

        SmaliParser parser = new SmaliParser();
        SmaliClass smaliClass = parser.parse(smaliText);

        assertThat(smaliClass).isNotNull();
        assertThat(smaliClass.getClassName()).isEqualTo("Lcom/example/MyClass;");
        assertThat(smaliClass.getSuperName()).isEqualTo("Ljava/lang/Object;");
        assertThat(smaliClass.getSourceFile()).isEqualTo("MyClass.java");
        assertThat(smaliClass.getInterfaces()).containsExactly("Ljava/lang/Runnable;");

        assertThat(smaliClass.getMethods()).hasSize(1);
        SmaliMethod method = smaliClass.getMethods().get(0);
        assertThat(method.getName()).isEqualTo("run");
        assertThat(method.getSignature()).isEqualTo("run()V");
        assertThat(method.getRegisters()).isEqualTo(2);
        assertThat(method.getInstructions()).containsExactly("nop");
    }
}
