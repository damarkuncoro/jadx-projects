package dexforge.core.parser.kotlin;

import static org.assertj.core.api.Assertions.assertThat;

import dexforge.core.parser.kotlin.model.KotlinClassInfo;
import org.junit.jupiter.api.Test;

class KotlinMetadataParserTest {

    @Test
    void testParseClassMetadata() {
        KotlinMetadataParser parser = new KotlinMetadataParser();

        // kind = 1 (Class)
        // d2[0] = name
        // d2[1..] = potential functions / properties
        String[] d1 = new String[]{};
        String[] d2 = new String[]{
                "com.example.MyClass",
                "myFunction",
                "someProp",
                "invalid$Method",
                "UpperName",
                "<init>"
        };

        KotlinClassInfo info = parser.parse(1, d1, d2);

        assertThat(info.getName()).isEqualTo("com.example.MyClass");
        assertThat(info.getFunctions()).containsExactly("myFunction");
        assertThat(info.getProperties()).containsExactly("someProp");
    }

    @Test
    void testParseNonClassMetadata() {
        KotlinMetadataParser parser = new KotlinMetadataParser();

        // kind = 2 (File) -> ignored in parsing logic
        String[] d1 = new String[]{};
        String[] d2 = new String[]{"MyFile", "myFunction"};

        KotlinClassInfo info = parser.parse(2, d1, d2);

        assertThat(info.getName()).isNull();
        assertThat(info.getFunctions()).isEmpty();
        assertThat(info.getProperties()).isEmpty();
    }
}
