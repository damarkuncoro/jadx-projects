package dexforge.engine.jadx.infrastructure;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import dexforge.core.application.decompile.DecompilePostLoadAction;
import dexforge.core.ports.decompile.DecompilerSession;
import jadx.api.JadxDecompiler;
import jadx.api.JavaClass;

public class JadxSingleClassDecompileAction implements DecompilePostLoadAction {
	private final String className;
	private final String outputPath;

	public JadxSingleClassDecompileAction(String className, String outputPath) {
		this.className = className;
		this.outputPath = outputPath;
	}

	@Override
	public boolean process(DecompilerSession session) {
		if (className == null) {
			return false;
		}
		JadxDecompiler decompiler = session.unwrap(JadxDecompiler.class);
		if (decompiler == null) {
			return false;
		}
		JavaClass javaClass = findClass(decompiler);
		if (javaClass == null) {
			throw new RuntimeException("Class not found: " + className);
		}
		String code = javaClass.getCode();
		if (outputPath != null) {
			saveToFile(code);
		} else {
			System.out.println(code);
		}
		return true;
	}

	private JavaClass findClass(JadxDecompiler decompiler) {
		JavaClass javaClass = decompiler.searchJavaClassByAliasFullName(className);
		if (javaClass == null) {
			javaClass = decompiler.searchJavaClassByOrigFullName(className);
		}
		return javaClass;
	}

	private void saveToFile(String code) {
		try {
			File outFile = new File(outputPath);
			File parent = outFile.getParentFile();
			if (parent != null && !parent.exists()) {
				parent.mkdirs();
			}
			Files.writeString(outFile.toPath(), code, StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new RuntimeException("Failed to save class code to file: " + outputPath, e);
		}
	}
}
