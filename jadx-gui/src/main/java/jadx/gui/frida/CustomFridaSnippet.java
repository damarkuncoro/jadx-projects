package jadx.gui.frida;

import com.google.gson.annotations.SerializedName;

public class CustomFridaSnippet {
	@SerializedName("name")
	private String name;
	@SerializedName("script")
	private String script;

	public CustomFridaSnippet() {
		// For deserialization
	}

	public CustomFridaSnippet(String name, String script) {
		this.name = name;
		this.script = script;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getScript() {
		return script;
	}

	public void setScript(String script) {
		this.script = script;
	}
}
