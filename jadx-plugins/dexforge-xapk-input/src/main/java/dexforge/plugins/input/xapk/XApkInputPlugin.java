package dexforge.plugins.input.xapk;

import dexforge.api.plugins.JadxPlugin;
import dexforge.api.plugins.JadxPluginContext;
import dexforge.api.plugins.JadxPluginInfo;
import dexforge.api.plugins.JadxPluginInfoBuilder;

public class XApkInputPlugin implements JadxPlugin {

	private XApkLoader loader;

	@Override
	public JadxPluginInfo getPluginInfo() {
		return JadxPluginInfoBuilder.pluginId("xapk-input")
				.name("XApk Input")
				.description("Load .xapk files")
				.build();
	}

	@Override
	public void init(JadxPluginContext context) {
		loader = new XApkLoader(context);
		XApkCustomCodeInput codeInput = new XApkCustomCodeInput(context, loader);
		XApkCustomResourcesLoader resourcesLoader = new XApkCustomResourcesLoader(context, loader);
		context.addCodeInput(codeInput);
		context.getDecompiler().addCustomResourcesLoader(resourcesLoader);
	}

	@Override
	public void unload() {
		if (loader != null) {
			loader.unload();
		}
	}
}
