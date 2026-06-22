package dexforge.api.plugins.pass.types;

import dexforge.api.plugins.pass.JadxPass;

import jadx.api.JadxDecompiler;

public interface JadxAfterLoadPass extends JadxPass {
	JadxPassType TYPE = new JadxPassType("AfterLoadPass");

	void init(JadxDecompiler decompiler);

	@Override
	default JadxPassType getPassType() {
		return TYPE;
	}
}
