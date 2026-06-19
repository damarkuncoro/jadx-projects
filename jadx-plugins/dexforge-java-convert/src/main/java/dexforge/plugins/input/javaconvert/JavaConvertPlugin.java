package dexforge.plugins.input.javaconvert;

import dexforge.api.plugins.JadxPlugin;
import dexforge.api.plugins.JadxPluginContext;
import dexforge.api.plugins.JadxPluginInfo;
import dexforge.api.plugins.JadxPluginInfoBuilder;
import dexforge.api.plugins.data.JadxPluginRuntimeData;
import dexforge.plugins.input.dex.DexInputPlugin;

public class JavaConvertPlugin implements JadxPlugin {
	public static final String PLUGIN_ID = "java-convert";

	private final JavaConvertOptions options = new JavaConvertOptions();

	private JadxPluginRuntimeData dexInput;
	private JavaConvertLoader loader;

	@Override
	public JadxPluginInfo getPluginInfo() {
		return JadxPluginInfoBuilder.pluginId(PLUGIN_ID)
				.name("Java Convert")
				.description("Convert .class, .jar and .aar files to dex")
				.provides("java-input")
				.build();
	}

	@Override
	public void init(JadxPluginContext context) {
		context.registerOptions(options);
		dexInput = context.plugins().getById(DexInputPlugin.PLUGIN_ID);
		loader = new JavaConvertLoader(options, context);
		context.addCodeInput(new JavaConvertCodeInput(loader, dexInput));
	}
}

