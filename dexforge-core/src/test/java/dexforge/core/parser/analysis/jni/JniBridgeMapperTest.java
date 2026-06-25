package dexforge.core.parser.analysis.jni;

import static org.assertj.core.api.Assertions.assertThat;

import dexforge.core.parser.native_lib.model.ElfSymbol;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class JniBridgeMapperTest {

    @Test
    void testMapSymbolToJava() {
        JniBridgeMapper mapper = new JniBridgeMapper();

        // Standard JNI Symbol
        assertThat(mapper.mapSymbolToJava("Java_com_example_app_NativeLib_stringFromJNI"))
                .isEqualTo("com.example.app.NativeLib.stringFromJNI");

        // Mangled JNI Symbol: _1 for _, _2 for ;, _3 for [
        assertThat(mapper.mapSymbolToJava("Java_com_example_app_NativeLib_some_1method_2_3"))
                .isEqualTo("com.example.app.NativeLib.some.method;[");

        // Non-JNI Symbol
        assertThat(mapper.mapSymbolToJava("someNormalFunction")).isNull();
    }

    @Test
    void testBuildBridgeMap() {
        JniBridgeMapper mapper = new JniBridgeMapper();

        List<ElfSymbol> symbols = new ArrayList<>();
        symbols.add(new ElfSymbol("Java_com_example_app_NativeLib_stringFromJNI", 0, true));
        symbols.add(new ElfSymbol("someNormalFunction", 0, false));

        Map<String, String> bridgeMap = mapper.buildBridgeMap(symbols);

        assertThat(bridgeMap).hasSize(1);
        assertThat(bridgeMap).containsEntry("com.example.app.NativeLib.stringFromJNI", "Java_com_example_app_NativeLib_stringFromJNI");
    }
}
