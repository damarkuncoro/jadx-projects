package dexforge.core.parser.resolver;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

class ResourceResolverTest {

    @Test
    void testResolveAndMapping() {
        ResourceResolver resolver = new ResourceResolver();

        // Check it starts with system resource mappings loaded from SystemResourceMap
        assertThat(resolver.resolve(0x01010003)).isEqualTo("android:name");

        // Custom mappings
        resolver.addMapping(0x7f010001, "R.string.custom_name");
        assertThat(resolver.resolve(0x7f010001)).isEqualTo("R.string.custom_name");

        Map<Integer, String> bulkMappings = new HashMap<>();
        bulkMappings.put(0x7f010002, "R.string.bulk1");
        bulkMappings.put(0x7f010003, "R.string.bulk2");
        resolver.addMappings(bulkMappings);

        assertThat(resolver.resolve(0x7f010002)).isEqualTo("R.string.bulk1");
        assertThat(resolver.resolve(0x7f010003)).isEqualTo("R.string.bulk2");
        assertThat(resolver.getAllMappings()).containsKeys(0x01010003, 0x7f010001, 0x7f010002, 0x7f010003);
    }

    @Test
    void testResolveOrDefault() {
        ResourceResolver resolver = new ResourceResolver();

        assertThat(resolver.resolveOrDefault(0x01010003)).isEqualTo("android:name");
        assertThat(resolver.resolveOrDefault(0x99999999)).isEqualTo("0x99999999");
    }

    @Test
    void testIsResourceId() {
        ResourceResolver resolver = new ResourceResolver();

        // Valid app resource ranges (0x7f010000 - 0x7fffffff)
        assertThat(resolver.isResourceId(0x7f010000)).isTrue();
        assertThat(resolver.isResourceId(0x7f123456)).isTrue();
        assertThat(resolver.isResourceId(0x7fffffff)).isTrue();

        // Valid system resource ranges (0x01010000 - 0x01ffffff)
        assertThat(resolver.isResourceId(0x01010000)).isTrue();
        assertThat(resolver.isResourceId(0x01ffffff)).isTrue();

        // Invalid ranges
        assertThat(resolver.isResourceId(0x00000000)).isFalse();
        assertThat(resolver.isResourceId(0x7f00ffff)).isFalse();
        assertThat(resolver.isResourceId(0x0100ffff)).isFalse();
        assertThat(resolver.isResourceId(0x80000000)).isFalse();
    }
}
