package dexforge.core.infrastructure.jadx;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dexforge.core.application.decompile.DecompilePostLoadAction;
import dexforge.core.application.decompile.SingleClassDecompileOptions;
import dexforge.core.ports.decompile.DecompilerSession;

import jadx.api.ICodeInfo;
import jadx.api.JadxDecompiler;
import jadx.core.dex.attributes.AFlag;
import jadx.core.dex.nodes.ClassNode;
import jadx.core.dex.visitors.SaveCode;
import jadx.core.utils.exceptions.JadxArgsValidateException;
import jadx.core.utils.exceptions.JadxRuntimeException;
import jadx.core.utils.files.FileUtils;

public final class JadxSingleClassDecompileAction implements DecompilePostLoadAction {
	private static final Logger LOG = LoggerFactory.getLogger(JadxSingleClassDecompileAction.class);

	private final SingleClassDecompileOptions options;

	public JadxSingleClassDecompileAction(SingleClassDecompileOptions options) {
		this.options = options;
	}

	@Override
	public boolean process(DecompilerSession session) {
		if (!options.isEnabled()) {
			return false;
		}
		return process(session.unwrap(JadxDecompiler.class));
	}

	private boolean process(JadxDecompiler decompiler) {
		ClassNode clsForProcess = resolveClassForProcess(decompiler);
		ICodeInfo codeInfo;
		try {
			codeInfo = clsForProcess.decompile();
		} catch (Exception e) {
			throw new JadxRuntimeException("Class decompilation failed", e);
		}
		String fileExt = SaveCode.getFileExtension(decompiler.getRoot());
		File resultOut = FileUtils.prepareFile(resolveOutputFile(decompiler, clsForProcess, fileExt));
		if (clsForProcess.getClassInfo().hasAlias()) {
			LOG.info("Saving class '{}' (alias: '{}') to file '{}'",
					clsForProcess.getClassInfo().getFullName(), clsForProcess.getFullName(), resultOut.getAbsolutePath());
		} else {
			LOG.info("Saving class '{}' to file '{}'", clsForProcess.getFullName(), resultOut.getAbsolutePath());
		}
		SaveCode.save(codeInfo.getCodeStr(), resultOut);
		return true;
	}

	private ClassNode resolveClassForProcess(JadxDecompiler decompiler) {
		String className = options.getClassName();
		if (className != null) {
			return resolveRequestedClass(decompiler, className);
		}
		List<ClassNode> classes = decompiler.getRoot().getClasses().stream()
				.filter(cls -> !cls.isInner() && !cls.contains(AFlag.DONT_GENERATE))
				.collect(Collectors.toList());
		int size = classes.size();
		if (size == 1) {
			return classes.get(0);
		}
		throw new JadxArgsValidateException("Found " + size + " classes, single class output can't be used");
	}

	private ClassNode resolveRequestedClass(JadxDecompiler decompiler, String className) {
		ClassNode clsForProcess = decompiler.getRoot().resolveClass(className);
		if (clsForProcess == null) {
			clsForProcess = decompiler.getRoot().getClasses().stream()
					.filter(cls -> cls.getClassInfo().getAliasFullName().equals(className))
					.findFirst().orElse(null);
		}
		if (clsForProcess == null) {
			throw new JadxArgsValidateException("Input class not found: " + className);
		}
		if (clsForProcess.contains(AFlag.DONT_GENERATE)) {
			throw new JadxArgsValidateException("Input class can't be saved by current jadx settings (marked as DONT_GENERATE)");
		}
		if (clsForProcess.isInner()) {
			clsForProcess = clsForProcess.getTopParentClass();
			LOG.warn("Input class is inner, parent class will be saved: {}", clsForProcess.getFullName());
		}
		return clsForProcess;
	}

	private File resolveOutputFile(JadxDecompiler decompiler, ClassNode clsForProcess, String fileExt) {
		String outputPath = options.getOutputPath();
		if (outputPath == null) {
			return new File(decompiler.getArgs().getOutDirSrc(), clsForProcess.getClassInfo().getAliasFullPath() + fileExt);
		}
		if (outputPath.endsWith(fileExt)) {
			return new File(outputPath);
		}
		return new File(outputPath, clsForProcess.getAlias() + fileExt);
	}
}
