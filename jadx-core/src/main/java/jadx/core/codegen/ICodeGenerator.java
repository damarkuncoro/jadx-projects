package jadx.core.codegen;

import jadx.api.ICodeInfo;
import jadx.core.dex.nodes.ClassNode;

public interface ICodeGenerator {
	ICodeInfo generate(ClassNode cls);
}
