package dexforge.core.parser.resolver;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SystemResourceMapTest {

    @Test
    void testGetSystemAttributes() {
        assertThat(SystemResourceMap.get(0x01010003)).isEqualTo("android:name");
        assertThat(SystemResourceMap.get(0x0101020c)).isEqualTo("android:debuggable");
        assertThat(SystemResourceMap.get(0x99999999)).isNull();

        assertThat(SystemResourceMap.getAll()).containsEntry(0x01010003, "android:name");
        assertThat(SystemResourceMap.getAll().size()).isGreaterThan(10);
    }
}
