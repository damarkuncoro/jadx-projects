package dexforge.plugins.input.apks

import dexforge.api.plugins.JadxPlugin
import dexforge.api.plugins.JadxPluginContext
import dexforge.api.plugins.JadxPluginInfo
import dexforge.plugins.input.dex.DexInputPlugin

class ApksInputPlugin : JadxPlugin {
	override fun getPluginInfo() = JadxPluginInfo(
		"apks-input",
		"APKS Input",
		"Load .apks files",
	)

	override fun init(context: JadxPluginContext) {
		val dexInputPlugin = context.plugins().getInstance(DexInputPlugin::class.java)
		context.addCodeInput(ApksCustomCodeInput(dexInputPlugin, context.zipReader))
		context.decompiler.addCustomResourcesLoader(ApksCustomResourcesLoader(context.zipReader))
	}
}
