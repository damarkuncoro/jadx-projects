package dexforge.plugins.input.aab.factories;

import java.io.IOException;
import java.io.InputStream;

import org.jetbrains.annotations.Nullable;

import dexforge.api.plugins.resources.IResContainerFactory;

import jadx.api.ICodeInfo;
import jadx.api.ResourceFile;
import jadx.api.impl.SimpleCodeInfo;
import jadx.core.xmlgen.ResContainer;

public class GenericProtoResContainerFactory implements IResContainerFactory {

	@FunctionalInterface
	public interface ProtoParser {
		Object parse(InputStream is) throws IOException;
	}

	private final String suffix;
	private final ProtoParser parser;

	public GenericProtoResContainerFactory(String suffix, ProtoParser parser) {
		this.suffix = suffix;
		this.parser = parser;
	}

	@Override
	public @Nullable ResContainer create(ResourceFile resFile, InputStream inputStream) throws IOException {
		if (!resFile.getOriginalName().endsWith(suffix)) {
			return null;
		}
		Object protoObj = parser.parse(inputStream);
		ICodeInfo content = new SimpleCodeInfo(protoObj.toString());
		return ResContainer.textResource(resFile.getDeobfName(), content);
	}
}
