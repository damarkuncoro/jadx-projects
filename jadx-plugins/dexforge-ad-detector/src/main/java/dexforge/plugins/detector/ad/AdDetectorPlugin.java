package dexforge.plugins.detector.ad;

import dexforge.api.plugins.JadxPlugin;
import dexforge.api.plugins.JadxPluginContext;
import dexforge.api.plugins.JadxPluginInfo;
import dexforge.api.plugins.JadxPluginInfoBuilder;

public class AdDetectorPlugin implements JadxPlugin {
	public static final String PLUGIN_ID = "ad-detector";

	@Override
	public JadxPluginInfo getPluginInfo() {
		return JadxPluginInfoBuilder.pluginId(PLUGIN_ID)
				.name("Ad Detector")
				.description("Detect tracker and ad networks inside decompiled classes")
				.build();
	}

	@Override
	public void init(JadxPluginContext context) {
		// Nothing to register on context for now, as it's triggered on-demand via GUI
	}
}
