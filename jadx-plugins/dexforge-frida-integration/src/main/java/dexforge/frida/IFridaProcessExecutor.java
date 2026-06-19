package dexforge.frida;

import java.io.IOException;

public interface IFridaProcessExecutor {
	void execute(String target, String script, LogListener logListener) throws IOException, InterruptedException;

	void stop();

	boolean isRunning();

	interface LogListener {
		void onLog(String line);
	}
}
