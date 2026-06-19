package dexforge.plugins.kotlin.smap

import dexforge.api.plugins.JadxPlugin
import dexforge.api.plugins.JadxPluginContext
import dexforge.api.plugins.JadxPluginInfo
import dexforge.plugins.kotlin.smap.pass.KotlinSourceDebugExtensionPass

class KotlinSmapPlugin : JadxPlugin {

	private val options = KotlinSmapOptions()

	override fun getPluginInfo(): JadxPluginInfo = JadxPluginInfo(PLUGIN_ID, "Kotlin SMAP", "Use kotlin.SourceDebugExtension annotation for rename class alias")

	override fun init(context: JadxPluginContext) {
		context.registerOptions(options)

		if (options.isClassSourceDbg()) {
			context.addPass(KotlinSourceDebugExtensionPass(options))
		}
	}

	companion object {
		const val PLUGIN_ID = "kotlin-smap"
	}
}
