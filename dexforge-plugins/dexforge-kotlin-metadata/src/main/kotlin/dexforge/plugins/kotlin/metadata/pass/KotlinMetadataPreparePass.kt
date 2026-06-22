package dexforge.plugins.kotlin.metadata.pass

import dexforge.api.plugins.pass.JadxPassInfo
import dexforge.api.plugins.pass.impl.OrderedJadxPassInfo
import dexforge.api.plugins.pass.types.JadxPreparePass
import dexforge.plugins.kotlin.metadata.KotlinMetadataOptions
import dexforge.plugins.kotlin.metadata.utils.KotlinMetadataUtils
import jadx.core.dex.attributes.AFlag
import jadx.core.dex.nodes.RootNode

class KotlinMetadataPreparePass(
	private val options: KotlinMetadataOptions,
) : JadxPreparePass {

	override fun getInfo(): JadxPassInfo = OrderedJadxPassInfo(
		"KotlinMetadataPrepare",
		"Use kotlin.Metadata annotation to rename class & package",
	)
		.before("RenameVisitor")

	override fun init(root: RootNode) {
		if (options.isClassAlias) {
			for (cls in root.classes) {
				if (cls.contains(AFlag.DONT_RENAME)) {
					continue
				}

				// rename class & package
				val kotlinCls = KotlinMetadataUtils.getAlias(cls)
				if (kotlinCls != null) {
					cls.rename(kotlinCls.name)
					cls.packageNode.rename(kotlinCls.pkg)
				}
			}
		}
	}
}
