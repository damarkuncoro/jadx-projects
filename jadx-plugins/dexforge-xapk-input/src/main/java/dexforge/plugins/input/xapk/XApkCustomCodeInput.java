package dexforge.plugins.input.xapk;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import dexforge.api.plugins.JadxPluginContext;
import dexforge.api.plugins.input.ICodeLoader;
import dexforge.api.plugins.input.JadxCodeInput;
import dexforge.api.plugins.input.data.impl.EmptyCodeLoader;
import dexforge.plugins.input.dex.DexInputPlugin;
import dexforge.plugins.input.xapk.data.XApkData;

public class XApkCustomCodeInput implements JadxCodeInput {
	private final JadxPluginContext context;
	private final XApkLoader loader;

	public XApkCustomCodeInput(JadxPluginContext context, XApkLoader loader) {
		this.context = context;
		this.loader = loader;
	}

	@Override
	public ICodeLoader loadFiles(List<Path> input) {
		List<Path> apks = new ArrayList<>();
		for (Path inputPath : input) {
			XApkData data = loader.checkAndLoad(inputPath);
			if (data != null) {
				apks.addAll(data.getApks());
			}
		}
		if (apks.isEmpty()) {
			return EmptyCodeLoader.INSTANCE;
		}
		DexInputPlugin dexInputPlugin = context.plugins().getInstance(DexInputPlugin.class);
		return dexInputPlugin.loadFiles(apks);
	}
}
