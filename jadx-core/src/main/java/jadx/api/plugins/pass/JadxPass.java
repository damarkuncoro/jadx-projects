package dexforge.api.plugins.pass;

import dexforge.api.plugins.pass.types.JadxPassType;

public interface JadxPass {
	JadxPassInfo getInfo();

	JadxPassType getPassType();
}
