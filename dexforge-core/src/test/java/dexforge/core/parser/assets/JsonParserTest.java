package dexforge.core.parser.assets;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import java.util.Map;

class JsonParserTest {

    @Test
    void testExtractPairs() {
        JsonParser parser = new JsonParser();
        String json = "{\n" +
                "  \"api_url\": \"https://api.example.com/v1\",\n" +
                "  \"timeout\": \"30\",\n" +
                "  \"ignored\": 123\n" +
                "}";

        Map<String, String> pairs = parser.extractPairs(json);
        assertThat(pairs).hasSize(2);
        assertThat(pairs).containsEntry("api_url", "https://api.example.com/v1");
        assertThat(pairs).containsEntry("timeout", "30");
    }

    @Test
    void testIsPotentialApiEndpoint() {
        JsonParser parser = new JsonParser();

        assertThat(parser.isPotentialApiEndpoint("https://api.example.com")).isTrue();
        assertThat(parser.isPotentialApiEndpoint("http://insecure.endpoint")).isTrue();
        assertThat(parser.isPotentialApiEndpoint("some-service.com/api")).isTrue();
        assertThat(parser.isPotentialApiEndpoint("another-service.io/v2")).isTrue();

        assertThat(parser.isPotentialApiEndpoint("not_an_endpoint")).isFalse();
        assertThat(parser.isPotentialApiEndpoint(null)).isFalse();
    }
}
