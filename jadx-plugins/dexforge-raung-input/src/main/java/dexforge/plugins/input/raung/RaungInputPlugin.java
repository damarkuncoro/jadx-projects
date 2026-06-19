package dexforge.plugins.input.raung;

import dexforge.api.plugins.JadxPlugin;
import dexforge.api.plugins.JadxPluginContext;
import dexforge.api.plugins.JadxPluginInfo;
import dexforge.api.plugins.data.JadxPluginRuntimeData;
import dexforge.api.plugins.input.data.impl.EmptyCodeLoader;

public class RaungInputPlugin implements JadxPlugin {

	@Override
	public JadxPluginInfo getPluginInfo() {
		return new JadxPluginInfo("raung-input", "Raung Input", "Load .raung files");
	}

	@Override
	public void init(JadxPluginContext context) {
		JadxPluginRuntimeData javaInput = context.plugins().getProviding("java-input");
		context.addCodeInput(inputs -> {
			RaungConvert convert = new RaungConvert();
			if (!convert.execute(inputs)) {
				return EmptyCodeLoader.INSTANCE;
			}
			return javaInput.loadCodeFiles(convert.getFiles(), convert);
		});
	}
}
