package dexforge.plugins.input.aab;

import org.jetbrains.annotations.Nullable;

import dexforge.api.plugins.resources.IResTableParserProvider;
import dexforge.plugins.input.aab.parsers.ResTableProtoParser;

import jadx.api.ResourceFile;
import jadx.core.dex.nodes.RootNode;
import jadx.core.xmlgen.IResTableParser;

public class ResTableProtoParserProvider implements IResTableParserProvider {
	private RootNode root;

	@Override
	public void init(RootNode root) {
		this.root = root;
	}

	@Override
	public @Nullable IResTableParser getParser(ResourceFile resFile) {
		String fileName = resFile.getOriginalName();
		if (!fileName.endsWith("resources.pb")) {
			return null;
		}
		return new ResTableProtoParser(root);
	}
}
