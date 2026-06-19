package dexforge.plugins.input.dex;

import dexforge.api.plugins.options.impl.BasePluginOptionsBuilder;

public class DexInputOptions extends BasePluginOptionsBuilder {

	private boolean verifyChecksum;

	@Override
	public void registerOptions() {
		boolOption(DexInputPlugin.PLUGIN_ID + ".verify-checksum")
				.description("verify dex file checksum before load")
				.defaultValue(true)
				.setter(v -> verifyChecksum = v);
	}

	public boolean isVerifyChecksum() {
		return verifyChecksum;
	}
}
