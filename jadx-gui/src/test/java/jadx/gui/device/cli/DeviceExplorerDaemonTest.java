package jadx.gui.device.cli;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import static org.assertj.core.api.Assertions.assertThat;

class DeviceExplorerDaemonTest {

	private java.io.InputStream originalIn;
	private PrintStream originalOut;
	private ByteArrayOutputStream testOut;

	@BeforeEach
	void setUp() {
		originalIn = System.in;
		originalOut = System.out;
		testOut = new ByteArrayOutputStream();
		System.setOut(new PrintStream(testOut, true, StandardCharsets.UTF_8));
	}

	@AfterEach
	void tearDown() {
		System.setIn(originalIn);
		System.setOut(originalOut);
	}

	private void setInput(String data) {
		System.setIn(new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8)));
	}

	@Test
	void testDaemonCommands() {
		String input = "{\"id\": 1, \"method\": \"list-devices\"}\n"
				+ "{\"id\": 2, \"method\": \"exit\"}\n";
		setInput(input);

		DeviceExplorerCLI.main(new String[] { "device-explorer", "daemon" });

		String output = testOut.toString(StandardCharsets.UTF_8);
		String[] lines = output.split("\n");

		// There should be two output lines: list-devices response, and exit response
		assertThat(lines).hasSize(2);

		// Verify first response
		JsonObject response1 = JsonParser.parseString(lines[0]).getAsJsonObject();
		assertThat(response1.get("id").getAsInt()).isEqualTo(1);
		assertThat(response1.get("status").getAsString()).isEqualTo("SUCCESS");
		assertThat(response1.get("result").isJsonArray()).isTrue();

		// Verify exit response
		JsonObject response2 = JsonParser.parseString(lines[1]).getAsJsonObject();
		assertThat(response2.get("id").getAsInt()).isEqualTo(2);
		assertThat(response2.get("status").getAsString()).isEqualTo("SUCCESS");
		assertThat(response2.get("result").getAsString()).isEqualTo("Daemon exiting");
	}

	@Test
	void testDaemonInvalidCommand() {
		String input = "{\"id\": 5, \"method\": \"non-existent-method\"}\n"
				+ "{\"id\": 6, \"method\": \"exit\"}\n";
		setInput(input);

		DeviceExplorerCLI.main(new String[] { "device-explorer", "daemon" });

		String output = testOut.toString(StandardCharsets.UTF_8);
		String[] lines = output.split("\n");

		assertThat(lines).hasSize(2);

		// Verify error response
		JsonObject response1 = JsonParser.parseString(lines[0]).getAsJsonObject();
		assertThat(response1.get("id").getAsInt()).isEqualTo(5);
		assertThat(response1.get("status").getAsString()).isEqualTo("ERROR");
		assertThat(response1.get("error").getAsJsonObject().get("code").getAsString()).isEqualTo("INTERNAL_ERROR");

		// Verify exit response
		JsonObject response2 = JsonParser.parseString(lines[1]).getAsJsonObject();
		assertThat(response2.get("id").getAsInt()).isEqualTo(6);
	}
}
