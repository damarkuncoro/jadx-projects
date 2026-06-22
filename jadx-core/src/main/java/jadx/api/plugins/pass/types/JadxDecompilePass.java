package dexforge.api.plugins.pass.types;

import dexforge.api.plugins.pass.JadxPass;

import jadx.core.dex.nodes.ClassNode;
import jadx.core.dex.nodes.MethodNode;
import jadx.core.dex.nodes.RootNode;

public interface JadxDecompilePass extends JadxPass {
	JadxPassType TYPE = new JadxPassType("DecompilePass");

	void init(RootNode root);

	/**
	 * Visit class
	 *
	 * @return false for disable child methods and inner classes traversal
	 */
	boolean visit(ClassNode cls);

	/**
	 * Visit method
	 */
	void visit(MethodNode mth);

	@Override
	default JadxPassType getPassType() {
		return TYPE;
	}
}
