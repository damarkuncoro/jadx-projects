package jadx.frida;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
			currentProcess = processBuilder.start();

			if (logListener != null) {
				logListener.onLog("[INFO] Starting Frida for target: " + target);
			}

			readProcessOutput(currentProcess, logListener);
			int exitCode = currentProcess.waitFor();

			if (logListener != null) {
				logListener.onLog("[INFO] Frida process exited with code: " + exitCode);
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
		if (target.equalsIgnoreCase("Gadget")) {
			command.add("-R");
		} else {
			command.add("-U");
			command.add("-f");
			command.add(target);
		}
		command.add("-l");
		command.add(tempScriptFile.toAbsolutePath().toString());
		return command;
	}

	private void readProcessOutput(Process process, LogListener logListener) throws IOException {
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (logListener != null) {
					logListener.onLog(line);
				}
			}
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
