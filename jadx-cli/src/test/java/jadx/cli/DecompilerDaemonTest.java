package jadx.cli;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import static org.assertj.core.api.Assertions.assertThat;

class DecompilerDaemonTest {
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

	private java.util.List<String> getJsonLines(String output) {
		java.util.List<String> jsonLines = new java.util.ArrayList<>();
		for (String line : output.split("\n")) {
			String trimmed = line.trim();
			if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
				jsonLines.add(trimmed);
			}
		}
		return jsonLines;
	}

	@Test
	void testDaemonInvalidCommands() {
		String input = "{\n" // Invalid JSON request format
				+ "{\"id\": 1, \"method\": \"unknown-method\"}\n"
				+ "{\"id\": 2, \"method\": \"exit\"}\n";
		setInput(input);

		JadxCLI.execute(new String[]{"decompiler-daemon"});

		String output = testOut.toString(StandardCharsets.UTF_8);
		java.util.List<String> lines = getJsonLines(output);

		assertThat(lines.size()).isGreaterThanOrEqualTo(2);

		// First response: Error for malformed JSON or unknown method
		JsonObject response1 = JsonParser.parseString(lines.get(0)).getAsJsonObject();
		assertThat(response1.get("status").getAsString()).isEqualTo("ERROR");

		// Last response: Exit
		JsonObject responseLast = JsonParser.parseString(lines.get(lines.size() - 1)).getAsJsonObject();
		assertThat(responseLast.get("id").getAsInt()).isEqualTo(2);
		assertThat(responseLast.get("status").getAsString()).isEqualTo("SUCCESS");
	}

	@Test
	void testDaemonLifecycle() throws Exception {
		// Resolve the absolute path of hello.dex
		URL resource = getClass().getClassLoader().getResource("samples/hello.dex");
		assertThat(resource).isNotNull();
		String sampleFile = resource.toURI().getPath();

		// JSON-RPC requests
		String input = "{\"id\": 1, \"method\": \"load\", \"params\": {\"path\": \"" + sampleFile + "\"}}\n"
				+ "{\"id\": 2, \"method\": \"list-classes\"}\n"
				+ "{\"id\": 5, \"method\": \"exit\"}\n";
		setInput(input);

		JadxCLI.execute(new String[]{"decompiler-daemon"});

		String output = testOut.toString(StandardCharsets.UTF_8);
		java.util.List<String> lines = getJsonLines(output);

		assertThat(lines).hasSize(3);

		// 1. Load response
		JsonObject loadResp = JsonParser.parseString(lines.get(0)).getAsJsonObject();
		assertThat(loadResp.get("id").getAsInt()).isEqualTo(1);
		assertThat(loadResp.get("status").getAsString()).isEqualTo("SUCCESS");
		JsonObject loadResult = loadResp.get("result").getAsJsonObject();
		assertThat(loadResult.get("classesCount").getAsInt()).isGreaterThan(0);

		// 2. List classes response
		JsonObject listResp = JsonParser.parseString(lines.get(1)).getAsJsonObject();
		assertThat(listResp.get("id").getAsInt()).isEqualTo(2);
		assertThat(listResp.get("status").getAsString()).isEqualTo("SUCCESS");
		JsonArray classes = listResp.get("result").getAsJsonArray();
		assertThat(classes.size()).isGreaterThan(0);

		// Get the first class info
		JsonObject firstClass = classes.get(0).getAsJsonObject();
		String fullName = firstClass.get("fullName").getAsString();

		// Let's run a second test targeting decompile and get-definition using the resolved fullName
		testOut.reset();
		String decompileInput = "{\"id\": 3, \"method\": \"load\", \"params\": {\"path\": \"" + sampleFile + "\"}}\n"
				+ "{\"id\": 4, \"method\": \"decompile\", \"params\": {\"className\": \"" + fullName + "\"}}\n"
				+ "{\"id\": 5, \"method\": \"get-definition\", \"params\": {\"className\": \"" + fullName + "\", \"pos\": 1}}\n"
				+ "{\"id\": 6, \"method\": \"exit\"}\n";
		setInput(decompileInput);

		JadxCLI.execute(new String[]{"decompiler-daemon"});

		output = testOut.toString(StandardCharsets.UTF_8);
		lines = getJsonLines(output);
		assertThat(lines).hasSize(4);

		// Verify decompile response
		JsonObject decompileResp = JsonParser.parseString(lines.get(1)).getAsJsonObject();
		assertThat(decompileResp.get("id").getAsInt()).isEqualTo(4);
		assertThat(decompileResp.get("status").getAsString()).isEqualTo("SUCCESS");
		JsonObject decompileResult = decompileResp.get("result").getAsJsonObject();
		assertThat(decompileResult.get("code").getAsString()).isNotEmpty();
		assertThat(decompileResult.has("diagnostics")).isTrue();
		assertThat(decompileResult.get("diagnostics").isJsonArray()).isTrue();

		// Verify get-definition response
		JsonObject getDefResp = JsonParser.parseString(lines.get(2)).getAsJsonObject();
		assertThat(getDefResp.get("id").getAsInt()).isEqualTo(5);
		// It could be either success (finding a node at pos 1) or error (if pos 1 has no node), but must not crash
		assertThat(getDefResp.has("status")).isTrue();
	}

	@Test
	void testLspCommands() throws Exception {
		URL resource = getClass().getClassLoader().getResource("samples/hello.dex");
		assertThat(resource).isNotNull();
		String sampleFile = resource.toURI().getPath();

		// Load and decompile to find a valid symbol position dynamically
		testOut.reset();
		String prepareInput = "{\"id\": 10, \"method\": \"load\", \"params\": {\"path\": \"" + sampleFile + "\"}}\n"
				+ "{\"id\": 11, \"method\": \"decompile\", \"params\": {\"className\": \"defpackage.HelloWorld\"}}\n"
				+ "{\"id\": 12, \"method\": \"exit\"}\n";
		setInput(prepareInput);
		JadxCLI.execute(new String[]{"lsp"});

		java.util.List<String> prepLines = getJsonLines(testOut.toString(StandardCharsets.UTF_8));
		JsonObject decompileResp = JsonParser.parseString(prepLines.get(1)).getAsJsonObject();
		String code = decompileResp.get("result").getAsJsonObject().get("code").getAsString();

		int idx = code.indexOf("class HelloWorld");
		if (idx == -1) {
			idx = code.indexOf("HelloWorld");
		}
		assertThat(idx).isNotEqualTo(-1);

		int line = 0;
		int character = 0;
		for (int i = 0; i < idx + 6; i++) { // position inside the class name definition
			if (code.charAt(i) == '\n') {
				line++;
				character = 0;
			} else {
				character++;
			}
		}

		testOut.reset();
		String input = "{\"id\": 1, \"method\": \"initialize\"}\n"
				+ "{\"id\": 2, \"method\": \"load\", \"params\": {\"path\": \"" + sampleFile + "\"}}\n"
				+ "{\"id\": 3, \"method\": \"textDocument/definition\", \"params\": {"
				+ "  \"textDocument\": {\"uri\": \"file:///sources/defpackage/HelloWorld.java\"},"
				+ "  \"position\": {\"line\": " + line + ", \"character\": " + character + "}"
				+ "}}\n"
				+ "{\"id\": 4, \"method\": \"textDocument/references\", \"params\": {"
				+ "  \"textDocument\": {\"uri\": \"file:///sources/defpackage/HelloWorld.java\"},"
				+ "  \"position\": {\"line\": " + line + ", \"character\": " + character + "}"
				+ "}}\n"
				+ "{\"id\": 5, \"method\": \"workspace/symbol\", \"params\": {\"query\": \"Hello\"}}\n"
				+ "{\"id\": 6, \"method\": \"textDocument/hover\", \"params\": {"
				+ "  \"textDocument\": {\"uri\": \"file:///sources/defpackage/HelloWorld.java\"},"
				+ "  \"position\": {\"line\": " + line + ", \"character\": " + character + "}"
				+ "}}\n"
				+ "{\"id\": 7, \"method\": \"exit\"}\n";
		setInput(input);

		JadxCLI.execute(new String[]{"lsp"});

		String output = testOut.toString(StandardCharsets.UTF_8);
		java.util.List<String> lines = getJsonLines(output);
		assertThat(lines).hasSize(7);

		// 1. Verify initialize response
		JsonObject initResp = JsonParser.parseString(lines.get(0)).getAsJsonObject();
		assertThat(initResp.get("id").getAsInt()).isEqualTo(1);
		assertThat(initResp.get("status").getAsString()).isEqualTo("SUCCESS");
		JsonObject capabilities = initResp.get("result").getAsJsonObject().get("capabilities").getAsJsonObject();
		assertThat(capabilities.get("definitionProvider").getAsBoolean()).isTrue();
		assertThat(capabilities.get("referencesProvider").getAsBoolean()).isTrue();
		assertThat(capabilities.get("workspaceSymbolProvider").getAsBoolean()).isTrue();
		assertThat(capabilities.get("hoverProvider").getAsBoolean()).isTrue();

		// 2. Verify load response
		JsonObject loadResp = JsonParser.parseString(lines.get(1)).getAsJsonObject();
		assertThat(loadResp.get("id").getAsInt()).isEqualTo(2);
		assertThat(loadResp.get("status").getAsString()).isEqualTo("SUCCESS");

		// 3. Verify textDocument/definition response
		JsonObject defResp = JsonParser.parseString(lines.get(2)).getAsJsonObject();
		assertThat(defResp.get("id").getAsInt()).isEqualTo(3);
		assertThat(defResp.get("status").getAsString()).isEqualTo("SUCCESS");
		JsonObject location = defResp.get("result").getAsJsonObject();
		assertThat(location.get("uri").getAsString()).isNotEmpty();
		assertThat(location.get("range").getAsJsonObject()).isNotNull();

		// 4. Verify textDocument/references response
		JsonObject refResp = JsonParser.parseString(lines.get(3)).getAsJsonObject();
		assertThat(refResp.get("id").getAsInt()).isEqualTo(4);
		assertThat(refResp.get("status").getAsString()).isEqualTo("SUCCESS");
		JsonArray references = refResp.get("result").getAsJsonArray();
		assertThat(references).isNotNull();

		// 5. Verify workspace/symbol response
		JsonObject symResp = JsonParser.parseString(lines.get(4)).getAsJsonObject();
		assertThat(symResp.get("id").getAsInt()).isEqualTo(5);
		assertThat(symResp.get("status").getAsString()).isEqualTo("SUCCESS");
		JsonArray symbols = symResp.get("result").getAsJsonArray();
		assertThat(symbols).isNotEmpty();
		JsonObject firstSymbol = symbols.get(0).getAsJsonObject();
		assertThat(firstSymbol.get("name").getAsString()).isNotEmpty();
		assertThat(firstSymbol.has("kind")).isTrue();
		assertThat(firstSymbol.has("location")).isTrue();

		// 6. Verify textDocument/hover response
		JsonObject hoverResp = JsonParser.parseString(lines.get(5)).getAsJsonObject();
		assertThat(hoverResp.get("id").getAsInt()).isEqualTo(6);
		assertThat(hoverResp.get("status").getAsString()).isEqualTo("SUCCESS");
		JsonObject hoverResult = hoverResp.get("result").getAsJsonObject();
		assertThat(hoverResult.has("contents")).isTrue();
		JsonObject hoverContents = hoverResult.get("contents").getAsJsonObject();
		assertThat(hoverContents.get("kind").getAsString()).isEqualTo("markdown");
		assertThat(hoverContents.get("value").getAsString()).contains("class defpackage.HelloWorld");
	}
}
