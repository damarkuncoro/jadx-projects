package dexforge.plugins.input.javaconvert;

import java.nio.file.Path;
import java.util.List;

import dexforge.api.plugins.data.JadxPluginRuntimeData;
import dexforge.api.plugins.input.ICodeLoader;
import dexforge.api.plugins.input.JadxCodeInput;
import dexforge.api.plugins.input.data.impl.EmptyCodeLoader;

public class JavaConvertCodeInput implements JadxCodeInput {

	private final JavaConvertLoader loader;
	private final JadxPluginRuntimeData dexInput;

	public JavaConvertCodeInput(JavaConvertLoader loader, JadxPluginRuntimeData dexInput) {
		this.loader = loader;
		this.dexInput = dexInput;
	}

	@Override
	public ICodeLoader loadFiles(List<Path> input) {
		ConvertResult result = loader.process(input);
		if (result.isEmpty()) {
			result.close();
			return EmptyCodeLoader.INSTANCE;
		}
		return dexInput.loadCodeFiles(result.getConverted(), result);
	}
}
