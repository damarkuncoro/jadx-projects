package dexforge.api.plugins.input;

import java.nio.file.Path;
import java.util.List;

public interface JadxCodeInput {
	ICodeLoader loadFiles(List<Path> input);
}
