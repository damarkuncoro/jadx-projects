package dexforge.core.parser.kotlin;

import dexforge.core.parser.kotlin.model.KotlinClassInfo;

/**
 * Foundation for parsing Kotlin Metadata annotations.
 * Decodes the 'd1' (data) and 'd2' (strings) fields.
 */
public final class KotlinMetadataParser {

    public KotlinClassInfo parse(int kind, String[] d1, String[] d2) {
        KotlinClassInfo info = new KotlinClassInfo();

        // kind 1 = Class, 2 = File, 3 = Synthetic, 4 = Multi-file part, 5 = Multi-file facade
        if (kind == 1) {
            // Process protobuf data from d1 using strings from d2
            if (d2.length > 0) info.setName(d2[0]);

            // Heuristic: identify potential functions and properties from the string pool
            for (int i = 1; i < d2.length; i++) {
                String s = d2[i];
                if (s.length() > 0 && Character.isLowerCase(s.charAt(0))) {
                    if (s.endsWith("Prop")) {
                        info.getProperties().add(s);
                    } else if (!s.contains("$") && !s.contains("<")) {
                        info.getFunctions().add(s);
                    }
                }
            }
        }

        return info;
    }
}
