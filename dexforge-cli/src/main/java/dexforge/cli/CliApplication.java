package dexforge.cli;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dexforge.cli.LogHelper.LogLevelEnum;
import dexforge.cli.plugins.DexforgeFilesGetter;
import dexforge.core.infrastructure.jadx.JadxBackedDexForgeEngine;
import dexforge.engine.DexForgeDecompileRequest;
import dexforge.engine.DexForgeDecompileResult;
import dexforge.engine.DexForgeEngine;
import dexforge.engine.DexForgeProgressReporter;

import jadx.api.JadxArgs;
import jadx.api.impl.AnnotatedCodeWriter;
import jadx.api.impl.NoOpCodeCache;
import jadx.api.impl.SimpleCodeWriter;
import jadx.api.usage.impl.EmptyUsageInfoCache;
import jadx.core.utils.exceptions.JadxArgsValidateException;
import jadx.plugins.tools.JadxExternalPluginsLoader;

public class CliApplication {
	private static final Logger LOG = LoggerFactory.getLogger(CliApplication.class);
	private static final String DEVICE_EXPLORER_CLI_CLASS = "jadx.gui.device.cli.DeviceExplorerCLI";

	private final CliCommandParser parser;

	public CliApplication(CliCommandParser parser) {
		this.parser = parser;
	}

	public int run(String[] args) {
		return run(args, null);
	}

	public int run(String[] args, @Nullable Consumer<JadxArgs> argsMod) {
		try {
			ParsedCommand parsedCommand = parser.parse(args);
			switch (parsedCommand.getType()) {
				case DEVICE_EXPLORER:
					return runDeviceExplorer(parsedCommand.getArgs());
				case DECOMPILER_DAEMON:
					return DecompilerDaemonCLI.run(parsedCommand.getArgs());
				case EXIT_SUCCESS:
					return 0;
				case DECOMPILE:
					DexforgeCLIArgs cliArgs = parsedCommand.getCliArgs();
					if (cliArgs == null) {
						return 0;
					}
					JadxArgs jadxArgs = buildArgs(cliArgs);
					if (argsMod != null) {
						argsMod.accept(jadxArgs);
					}
					return runSave(jadxArgs, cliArgs).getExitCode();
				default:
					return 1;
			}
		} catch (JadxArgsValidateException e) {
			LOG.error("Incorrect arguments: {}", e.getMessage());
			return 1;
		} catch (Throwable e) {
			LOG.error("Process error:", e);
			return 1;
		}
	}

	private int runDeviceExplorer(String[] args) {
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

	private JadxArgs buildArgs(DexforgeCLIArgs cliArgs) {
		JadxArgs jadxArgs = cliArgs.toJadxArgs();
		jadxArgs.setCodeCache(new NoOpCodeCache());
		jadxArgs.setUsageInfoCache(new EmptyUsageInfoCache());
		jadxArgs.setPluginLoader(new JadxExternalPluginsLoader());
		jadxArgs.setFilesGetter(DexforgeFilesGetter.INSTANCE);
		initCodeWriterProvider(jadxArgs);
		DexforgeAppCommon.applyEnvVars(jadxArgs);
		return jadxArgs;
	}

	private void initCodeWriterProvider(JadxArgs jadxArgs) {
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

	private DexForgeDecompileResult runSave(JadxArgs jadxArgs, DexforgeCLIArgs cliArgs) {
		DexForgeEngine engine = JadxBackedDexForgeEngine.create(jadxArgs);
		DexForgeDecompileRequest request = DexForgeDecompileRequest.builder()
				.quiet(LogHelper.getLogLevel() == LogLevelEnum.QUIET)
				.progressReporter(new CliProgressReporter())
				.singleClass(cliArgs.getSingleClass(), cliArgs.getSingleClassOutput())
				.build();
		try (var session = engine.openSession()) {
			return session.decompile(request);
		}
	}

	private static final class CliProgressReporter implements DexForgeProgressReporter {
		@Override
		public void onProgress(long done, long total) {
			int progress = (int) (done * 100.0 / total);
			System.out.printf("INFO  - progress: %d of %d (%d%%)\r", done, total, progress);
		}

		@Override
		public void clear() {
			System.out.print("                                                             \r");
		}
	}
}
