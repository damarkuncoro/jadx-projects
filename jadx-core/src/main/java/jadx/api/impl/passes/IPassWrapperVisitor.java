package jadx.api.impl.passes;

import dexforge.api.plugins.pass.JadxPass;

import jadx.core.dex.visitors.IDexTreeVisitor;

public interface IPassWrapperVisitor extends IDexTreeVisitor {

	JadxPass getPass();
}
