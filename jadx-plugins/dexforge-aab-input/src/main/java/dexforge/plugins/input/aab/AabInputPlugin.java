package dexforge.plugins.input.aab;

import com.android.bundle.AppDependenciesOuterClass;
import com.android.bundle.Config.BundleConfig;
import com.android.bundle.Files;

import dexforge.api.plugins.JadxPlugin;
import dexforge.api.plugins.JadxPluginContext;
import dexforge.api.plugins.JadxPluginInfo;
import dexforge.api.plugins.resources.IResourcesLoader;
import dexforge.plugins.input.aab.factories.GenericProtoResContainerFactory;
import dexforge.plugins.input.aab.factories.ProtoTableResContainerFactory;
import dexforge.plugins.input.aab.factories.ProtoXmlResContainerFactory;

public class AabInputPlugin implements JadxPlugin {
	public static final String PLUGIN_ID = "aab-input";

	@Override
	public JadxPluginInfo getPluginInfo() {
		return new JadxPluginInfo(
				PLUGIN_ID,
				".AAB Input",
				"Loads .AAB files.");
	}

	@Override
	public synchronized void init(JadxPluginContext context) {
		IResourcesLoader resourcesLoader = context.getResourcesLoader();
		ResTableProtoParserProvider tableParserProvider = new ResTableProtoParserProvider();
		resourcesLoader.addResTableParserProvider(tableParserProvider);

		resourcesLoader.addResContainerFactory(new ProtoTableResContainerFactory(tableParserProvider));
		resourcesLoader.addResContainerFactory(new ProtoXmlResContainerFactory());
		resourcesLoader.addResContainerFactory(new GenericProtoResContainerFactory(
				"BundleConfig.pb", BundleConfig::parseFrom));
		resourcesLoader.addResContainerFactory(new GenericProtoResContainerFactory(
				"assets.pb", Files.Assets::parseFrom));
		resourcesLoader.addResContainerFactory(new GenericProtoResContainerFactory(
				"native.pb", Files.NativeLibraries::parseFrom));
		resourcesLoader.addResContainerFactory(new GenericProtoResContainerFactory(
				"BUNDLE-METADATA/com.android.tools.build.libraries/dependencies.pb",
				AppDependenciesOuterClass.AppDependencies::parseFrom));
	}
}
