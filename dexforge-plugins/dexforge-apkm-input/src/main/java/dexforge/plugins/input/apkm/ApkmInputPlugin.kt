package dexforge.plugins.input.apkm

import dexforge.api.plugins.JadxPlugin
import dexforge.api.plugins.JadxPluginContext
import dexforge.api.plugins.JadxPluginInfo
import dexforge.plugins.input.dex.DexInputPlugin

class ApkmInputPlugin : JadxPlugin {

	override fun getPluginInfo() = JadxPluginInfo(
		"apkm-input",
		"APKM Input",
		"Load .apkm files",
	)

	override fun init(context: JadxPluginContext) {
		val dexInputPlugin = context.plugins().getInstance(DexInputPlugin::class.java)
		context.addCodeInput(ApkmCustomCodeInput(dexInputPlugin, context.zipReader))
		context.decompiler.addCustomResourcesLoader(ApkmCustomResourcesLoader(context.zipReader))
	}
}
