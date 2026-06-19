package dexforge.plugins.input.javaconvert;

import dexforge.api.plugins.options.impl.BasePluginOptionsBuilder;

import static dexforge.plugins.input.javaconvert.JavaConvertPlugin.PLUGIN_ID;

public class JavaConvertOptions extends BasePluginOptionsBuilder {

	public enum Mode {
		DX, D8, BOTH
	}

	private Mode mode;
	private boolean d8Desugar;

	@Override
	public void registerOptions() {
		enumOption(PLUGIN_ID + ".mode", Mode.values(), Mode::valueOf)
				.description("convert mode")
				.defaultValue(Mode.BOTH)
				.setter(v -> mode = v);

		boolOption(PLUGIN_ID + ".d8-desugar")
				.description("use desugar in d8")
				.defaultValue(false)
				.setter(v -> d8Desugar = v);
	}

	public Mode getMode() {
		return mode;
	}

	public boolean isD8Desugar() {
		return d8Desugar;
	}
}
