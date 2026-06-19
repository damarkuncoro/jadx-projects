package dexforge.plugins.kotlin.metadata

import dexforge.api.plugins.JadxPlugin
import dexforge.api.plugins.JadxPluginContext
import dexforge.api.plugins.JadxPluginInfo
import dexforge.plugins.kotlin.metadata.pass.KotlinMetadataDecompilePass
import dexforge.plugins.kotlin.metadata.pass.KotlinMetadataPreparePass

class KotlinMetadataPlugin : JadxPlugin {

	private val options = KotlinMetadataOptions()

	override fun getPluginInfo(): JadxPluginInfo = JadxPluginInfo(PLUGIN_ID, "Kotlin Metadata", "Use kotlin.Metadata annotation for code generation")

	override fun init(context: JadxPluginContext) {
		context.registerOptions(options)
		if (options.isPreparePassNeeded()) {
			context.addPass(KotlinMetadataPreparePass(options))
		}
		if (options.isDecompilePassNeeded()) {
			context.addPass(KotlinMetadataDecompilePass(options))
		}
	}

	companion object {
		const val PLUGIN_ID = "kotlin-metadata"
	}
}
