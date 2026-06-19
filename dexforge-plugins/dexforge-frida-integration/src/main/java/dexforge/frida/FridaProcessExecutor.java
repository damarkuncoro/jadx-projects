package dexforge.frida;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FridaProcessExecutor implements IFridaProcessExecutor {
	private static final Logger LOG = LoggerFactory.getLogger(FridaProcessExecutor.class);
	private static final int OUTPUT_CONNECTION_TERMINATED = 1;
	private static final int OUTPUT_FAILED_TO_SPAWN = 1 << 1;

	private Process currentProcess;
	private final AtomicBoolean isRunning = new AtomicBoolean(false);

	@Override
	public void execute(String target, String script, LogListener logListener) throws IOException, InterruptedException {
		if (isRunning.get()) {
			throw new IllegalStateException("Frida process is already running");
		}

		isRunning.set(true);
		Path tempScriptFile = null;
		try {
			tempScriptFile = Files.createTempFile("jadx_frida_", ".js");
			Files.write(tempScriptFile, script.getBytes(StandardCharsets.UTF_8));

			List<String> command = buildCommand(target, tempScriptFile);
			ProcessBuilder processBuilder = new ProcessBuilder(command);
			processBuilder.redirectErrorStream(true);
			processBuilder.redirectInput(ProcessBuilder.Redirect.DISCARD);
			if (logListener != null) {
				logListener.onLog("[INFO] Frida command: " + formatCommand(command));
			}
			currentProcess = processBuilder.start();
			closeProcessInput(currentProcess);

			if (logListener != null) {
				logListener.onLog("[INFO] Starting Frida for target: " + target);
			}

			int outputStatus = readProcessOutput(currentProcess, logListener);
			int exitCode = currentProcess.waitFor();

			if (logListener != null) {
				logListener.onLog("[INFO] Frida process exited with code: " + exitCode);
				logFridaExitHints(target, exitCode, outputStatus, logListener);
			}
		} finally {
			cleanup(tempScriptFile);
			isRunning.set(false);
			currentProcess = null;
		}
	}

	private List<String> buildCommand(String target, Path tempScriptFile) {
		List<String> command = new ArrayList<>();
		command.add("frida");
		command.add("-q");
		command.add("-t");
		command.add("inf");
		if (target.equalsIgnoreCase("Gadget")) {
			command.add("-R");
			command.add("-n");
			command.add("Gadget");
		} else {
			command.add("-U");
			command.add("-f");
			command.add(target);
		}
		command.add("-l");
		command.add(tempScriptFile.toAbsolutePath().toString());
		return command;
	}

	private String formatCommand(List<String> command) {
		StringBuilder formatted = new StringBuilder();
		for (String arg : command) {
			if (formatted.length() != 0) {
				formatted.append(' ');
			}
			formatted.append(quoteCommandArg(arg));
		}
		return formatted.toString();
	}

	private String quoteCommandArg(String arg) {
		if (arg.matches("[A-Za-z0-9_./:=@%+-]+")) {
			return arg;
		}
		return "'" + arg.replace("'", "'\"'\"'") + "'";
	}

	private void closeProcessInput(Process process) {
		try {
			OutputStream input = process.getOutputStream();
			input.close();
		} catch (IOException e) {
			LOG.debug("Failed to close Frida process input", e);
		}
	}

	private int readProcessOutput(Process process, LogListener logListener) throws IOException {
		int outputStatus = 0;
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
			String line;
			while ((line = reader.readLine()) != null) {
				String lowerLine = line.toLowerCase();
				if (lowerLine.contains("connection terminated")) {
					outputStatus |= OUTPUT_CONNECTION_TERMINATED;
				}
				if (lowerLine.contains("failed to spawn")) {
					outputStatus |= OUTPUT_FAILED_TO_SPAWN;
				}
				if (logListener != null) {
					logListener.onLog(line);
				}
			}
		}
		return outputStatus;
	}

	private void logFridaExitHints(String target, int exitCode, int outputStatus, LogListener logListener) {
		if ((outputStatus & OUTPUT_FAILED_TO_SPAWN) != 0) {
			logListener.onLog(
					"[HINT] Frida failed before the script was attached. For non-rooted devices, install/open the patched APK first, then attach to Gadget.");
		}
		if ((outputStatus & OUTPUT_CONNECTION_TERMINATED) != 0) {
			logListener.onLog(
					"[HINT] The Frida connection was terminated by the target or Gadget. Check adb logcat for app crashes, anti-tamper exits, or libfrida-gadget load errors.");
		}
		if (exitCode != 0 && "Gadget".equalsIgnoreCase(target)) {
			logListener.onLog(
					"[HINT] Gadget mode expects the patched app to be running and listening on the forwarded port before attaching.");
		}
	}

	private void cleanup(Path tempScriptFile) {
		if (tempScriptFile != null) {
			try {
				Files.deleteIfExists(tempScriptFile);
			} catch (IOException e) {
				LOG.warn("Failed to delete temp script file", e);
			}
		}
	}

	@Override
	public void stop() {
		if (currentProcess != null && currentProcess.isAlive()) {
			currentProcess.destroy();
		}
	}

	@Override
	public boolean isRunning() {
		return isRunning.get();
	}
}
