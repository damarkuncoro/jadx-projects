package dexforge.api.plugins.pass.types;

import jadx.api.JadxDecompiler;
import dexforge.api.plugins.pass.JadxPass;

public interface JadxAfterLoadPass extends JadxPass {
	JadxPassType TYPE = new JadxPassType("AfterLoadPass");

	void init(JadxDecompiler decompiler);

	@Override
	default JadxPassType getPassType() {
		return TYPE;
	}
}
