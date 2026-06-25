package dexforge.core.parser.analysis.jni;

import dexforge.core.parser.native_lib.model.ElfSymbol;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Maps Java native method signatures to their corresponding C/C++ implementations in ELF files.
 */
public final class JniBridgeMapper {

    /**
     * Translates an ELF JNI symbol name back to a Java class/method signature.
     * Example: Java_com_example_app_NativeLib_stringFromJNI
     * Result: com.example.app.NativeLib.stringFromJNI
     */
    public String mapSymbolToJava(String symbol) {
        if (!symbol.startsWith("Java_")) return null;

        // Remove Java_ prefix
        String parts = symbol.substring(5);

        // Handle name mangling: _1 for _, _2 for ;, _3 for [
        parts = parts.replace("_1", "_")
                     .replace("_2", ";")
                     .replace("_3", "[");

        // Split by underscores to find class and method
        // This is a heuristic; actual mangling is more complex for overloaded methods
        int lastUnderscore = parts.lastIndexOf('_');
        if (lastUnderscore == -1) return parts;

        String className = parts.substring(0, lastUnderscore).replace('_', '.');
        String methodName = parts.substring(lastUnderscore + 1);

        return className + "." + methodName;
    }

    public Map<String, String> buildBridgeMap(List<ElfSymbol> symbols) {
        Map<String, String> bridgeMap = new HashMap<>();
        for (ElfSymbol symbol : symbols) {
            if (symbol.isJniMethod()) {
                String javaName = mapSymbolToJava(symbol.getName());
                if (javaName != null) {
                    bridgeMap.put(javaName, symbol.getName());
                }
            }
        }
        return bridgeMap;
    }
}
