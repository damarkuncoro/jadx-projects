package jadx.cli;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jadx.api.JadxArgs;
import jadx.api.JadxDecompiler;
import jadx.api.impl.AnnotatedCodeWriter;
import jadx.api.impl.NoOpCodeCache;
import jadx.api.impl.SimpleCodeWriter;
import jadx.api.usage.impl.EmptyUsageInfoCache;
import jadx.cli.LogHelper.LogLevelEnum;
import jadx.cli.config.JadxConfigAdapter;
import jadx.cli.plugins.JadxFilesGetter;
import jadx.core.utils.exceptions.JadxArgsValidateException;
import jadx.plugins.tools.JadxExternalPluginsLoader;

public class JadxCLI {
	private static final Logger LOG = LoggerFactory.getLogger(JadxCLI.class);
	private static final String DEVICE_EXPLORER_COMMAND = "device-explorer";
	private static final String DEVICE_EXPLORER_CLI_CLASS = "jadx.gui.device.cli.DeviceExplorerCLI";

	public static void main(String[] args) {
		int result = 1;
		try {
			result = execute(args);
		} finally {
			System.exit(result);
		}
	}

	public static int execute(String[] args) {
		return execute(args, null);
	}

	public static int execute(String[] args, @Nullable Consumer<JadxArgs> argsMod) {
		try {
			if (isDeviceExplorerCommand(args)) {
				return runDeviceExplorer(args);
			}
			if (isDecompilerDaemonCommand(args)) {
				return DecompilerDaemonCLI.run(args);
			}
			JadxCLIArgs cliArgs = JadxCLIArgs.processArgs(args,
					new JadxCLIArgs(),
					new JadxConfigAdapter<>(JadxCLIArgs.class, "cli"));
			if (cliArgs == null) {
				return 0;
			}
			JadxArgs jadxArgs = buildArgs(cliArgs);
			if (argsMod != null) {
				argsMod.accept(jadxArgs);
			}
			return runSave(jadxArgs, cliArgs);
		} catch (JadxArgsValidateException e) {
			LOG.error("Incorrect arguments: {}", e.getMessage());
			return 1;
		} catch (Throwable e) {
			LOG.error("Process error:", e);
			return 1;
		}
	}

	private static boolean isDeviceExplorerCommand(String[] args) {
		return args.length > 0 && DEVICE_EXPLORER_COMMAND.equals(args[0]);
	}

	private static boolean isDecompilerDaemonCommand(String[] args) {
		return args.length > 0 && ("decompiler-daemon".equals(args[0]) || "lsp".equals(args[0]));
	}

	private static int runDeviceExplorer(String[] args) {
		try {
			Class<?> cliClass = Class.forName(DEVICE_EXPLORER_CLI_CLASS);
			Method mainMethod = cliClass.getMethod("main", String[].class);
			mainMethod.invoke(null, (Object) args);
			return 0;
		} catch (ClassNotFoundException e) {
			LOG.error("Device Explorer is not available in this distribution. Use the DexForge Engine bundle.");
			return 1;
		} catch (NoSuchMethodException | IllegalAccessException e) {
			LOG.error("Failed to start Device Explorer CLI", e);
			return 1;
		} catch (InvocationTargetException e) {
			Throwable cause = e.getCause();
			LOG.error("Device Explorer CLI failed", cause == null ? e : cause);
			return 1;
		}
	}

	private static JadxArgs buildArgs(JadxCLIArgs cliArgs) {
		JadxArgs jadxArgs = cliArgs.toJadxArgs();
		jadxArgs.setCodeCache(new NoOpCodeCache());
		jadxArgs.setUsageInfoCache(new EmptyUsageInfoCache());
		jadxArgs.setPluginLoader(new JadxExternalPluginsLoader());
		jadxArgs.setFilesGetter(JadxFilesGetter.INSTANCE);
		initCodeWriterProvider(jadxArgs);
		JadxAppCommon.applyEnvVars(jadxArgs);
		return jadxArgs;
	}

	private static int runSave(JadxArgs jadxArgs, JadxCLIArgs cliArgs) {
		try (JadxDecompiler jadx = new JadxDecompiler(jadxArgs)) {
			jadx.load();
			if (checkForErrors(jadx)) {
				return 2;
			}
			if (!SingleClassMode.process(jadx, cliArgs)) {
				save(jadx);
			}
			int errorsCount = jadx.getErrorsCount();
			if (errorsCount != 0) {
				jadx.printErrorsReport();
				LOG.error("finished with errors, count: {}", errorsCount);
				return 3;
			}
			LOG.info("done");
			return 0;
		}
	}

	private static void initCodeWriterProvider(JadxArgs jadxArgs) {
		switch (jadxArgs.getOutputFormat()) {
			case JAVA:
				jadxArgs.setCodeWriterProvider(SimpleCodeWriter::new);
				break;
			case JSON:
				// needed for code offsets and source lines
				jadxArgs.setCodeWriterProvider(AnnotatedCodeWriter::new);
				break;
		}
	}

	private static boolean checkForErrors(JadxDecompiler jadx) {
		if (jadx.getRoot().getClasses().isEmpty()) {
			if (jadx.getArgs().isSkipResources()) {
				LOG.error("Load failed! No classes for decompile!");
				return true;
			}
			if (!jadx.getArgs().isSkipSources()) {
				LOG.warn("No classes to decompile; decoding resources only");
				jadx.getArgs().setSkipSources(true);
			}
		}
		int errorsCount = jadx.getErrorsCount();
		if (errorsCount > 0) {
			LOG.error("Loading finished with errors! Count: {}", errorsCount);
			// continue processing
		}
		return false;
	}

	private static void save(JadxDecompiler jadx) {
		if (LogHelper.getLogLevel() == LogLevelEnum.QUIET) {
			jadx.save();
		} else {
			LOG.info("processing ...");
			jadx.save(500, (done, total) -> {
				int progress = (int) (done * 100.0 / total);
				System.out.printf("INFO  - progress: %d of %d (%d%%)\r", done, total, progress);
			});
			// dumb line clear :)
			System.out.print("                                                             \r");
		}
	}
}
